package com.example.ticketsystem.service.impl;


import com.example.ticketsystem.dto.TicketNotifyMessage;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.dto.*;
import com.example.ticketsystem.entity.OperationLog;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.mapper.OperationLogMapper;
import com.example.ticketsystem.mapper.TicketMapper;
import com.example.ticketsystem.mapper.TicketStatsMapper;
import com.example.ticketsystem.service.MessageProducer;
import com.example.ticketsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ticketsystem.dto.CacheDeleteMessage;
import io.micrometer.core.instrument.Counter;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final StringRedisTemplate redisTemplate;
    private final TicketMapper ticketMapper;
    private final OperationLogMapper operationLogMapper;
    private final TicketStatsMapper ticketStatsMapper;
    private final MessageProducer messageProducer;
    private final Counter ticketCreatedCounter;

    @Override
    public TicketVO getTicketById(Long id, Long userId, List<String> roles) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 数据权限校验
        if (!roles.contains("admin")) {
            boolean isCreator = ticket.getCreatorId().equals(userId);
            boolean isAssignee = userId.equals(ticket.getAssigneeId());
            if (!isCreator && !isAssignee) {
                throw new BusinessException(403, "无权查看此工单");
            }
        }

        return toVO(ticket);
    }

    @Override
    public IPage<TicketVO> getTicketList(Long userId, List<String> roles, int page, int size) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        if (roles.contains("admin")) {
            // 管理员：不加任何过滤，看全部
        } else if (roles.contains("handler")) {
            // 处理人：看分配给自己的
            wrapper.eq(Ticket::getAssigneeId, userId);
        } else {
            // 普通用户：只看自己创建的
            wrapper.eq(Ticket::getCreatorId, userId);
        }

        wrapper.orderByDesc(Ticket::getCreateTime);

        IPage<Ticket> ticketPage = ticketMapper.selectPage(new Page<>(page, size), wrapper);
        return ticketPage.convert(this::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketVO createTicket(TicketCreateRequest request, Long creatorId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(401, "租户上下文缺失");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        ticket.setStatus(TicketStatus.PENDING.name());
        ticket.setCreatorId(creatorId);
        ticket.setTenantId(tenantId);
        ticket.setSlaDeadline(LocalDateTime.now().plusHours(24));

        // 数据库操作（事务内）
        ticketMapper.insert(ticket);
        ticketStatsMapper.increment(tenantId, TicketStatus.PENDING.name());

        // 事务提交成功后再执行 Redis / MQ / 计数器
        Long currentTenantId = tenantId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.opsForHash().increment(
                        "ticket:stats:" + currentTenantId, TicketStatus.PENDING.name(), 1);

                TicketNotifyMessage notifyMsg = new TicketNotifyMessage(
                        ticket.getId(), ticket.getTitle(), "CREATED",
                        null, TicketStatus.PENDING.name(),
                        creatorId, null
                );
                messageProducer.sendTicketCreatedNotify(notifyMsg);

                ticketCreatedCounter.increment();
            }
        });

        return toVO(ticket);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketVO transitStatus(Long id, String targetStatus, Long userId, List<String> roles) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 数据权限校验：管理员可操作全部，其余仅限创建人或已分配的处理人
        if (!roles.contains("admin")) {
            boolean isCreator = ticket.getCreatorId().equals(userId);
            boolean isAssignee = userId.equals(ticket.getAssigneeId());
            if (!isCreator && !isAssignee) {
                throw new BusinessException(403, "无权操作此工单");
            }
        }

        TicketStatus currentStatus;
        TicketStatus newStatus;
        try {
            currentStatus = TicketStatus.valueOf(ticket.getStatus());
            newStatus = TicketStatus.valueOf(targetStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的状态值: " + targetStatus);
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException(400,
                    String.format("不允许从 %s 转换到 %s", currentStatus, newStatus));
        }

        // 第1步：更新工单状态
        String oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus.name());
        if (newStatus == TicketStatus.PROCESSING && ticket.getAssigneeId() == null) {
            ticket.setAssigneeId(userId);
        }
        ticketMapper.updateById(ticket);

        // 第2步：记录操作日志
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperation("STATUS_CHANGE");
        log.setTargetType("TICKET");
        log.setTargetId(id);
        log.setDetail(oldStatus + " -> " + newStatus.name());
        operationLogMapper.insert(log);

        // 第3步：更新统计数据（数据库）
        Long currentTenantId = TenantContext.getTenantId();
        ticketStatsMapper.decrement(currentTenantId, oldStatus);
        ticketStatsMapper.increment(currentTenantId, newStatus.name());

        // 事务提交成功后再执行 Redis / MQ
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 更新 Redis 缓存
                String statsKey = "ticket:stats:" + currentTenantId;
                try {
                    redisTemplate.opsForHash().increment(statsKey, oldStatus, -1);
                    redisTemplate.opsForHash().increment(statsKey, newStatus.name(), 1);
                } catch (Exception e) {
                    TicketServiceImpl.log.error("Redis缓存更新失败，发送MQ补偿", e);
                    messageProducer.sendCacheDeleteMessage(new CacheDeleteMessage(statsKey, 0));
                }

                // 异步通知创建人
                TicketNotifyMessage notifyMsg = new TicketNotifyMessage(
                        ticket.getId(), ticket.getTitle(), "STATUS_CHANGED",
                        oldStatus, newStatus.name(),
                        userId, ticket.getCreatorId()
                );
                messageProducer.sendTicketStatusChangedNotify(notifyMsg);
            }
        });

        return toVO(ticket);
    }

    @Override
    public TicketVO getTicketById(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }
        return toVO(ticket);
    }

    @Override
    public IPage<TicketVO> getMyTickets(Long userId, int page, int size) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ticket::getCreatorId, userId)
                .orderByDesc(Ticket::getCreateTime);

        IPage<Ticket> ticketPage = ticketMapper.selectPage(new Page<>(page, size), wrapper);

        return ticketPage.convert(this::toVO);
    }

    @Override
    public TicketVO updateTicket(Long id, TicketUpdateRequest request, Long userId) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }
        // 只有创建人才能修改
        if (!ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权修改此工单");
        }

        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getAssigneeId() != null) ticket.setAssigneeId(request.getAssigneeId());

        ticketMapper.updateById(ticket);
        return toVO(ticket);
    }

    private TicketVO toVO(Ticket ticket) {
        TicketVO vo = new TicketVO();
        BeanUtils.copyProperties(ticket, vo);
        return vo;
    }
}
