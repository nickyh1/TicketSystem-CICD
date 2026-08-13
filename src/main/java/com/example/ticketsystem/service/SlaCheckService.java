package com.example.ticketsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.dto.TicketNotifyMessage;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.mapper.SysTenantMapper;
import com.example.ticketsystem.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaCheckService {

    private final TicketMapper ticketMapper;
    private final MessageProducer messageProducer;
    private final SysTenantMapper sysTenantMapper;

    /**
     * 每5分钟扫描一次即将超时的工单，遍历所有启用租户
     */
    @Scheduled(fixedRateString = "${ticket.sla.check-interval-ms:300000}")
    public void checkSlaDeadline() {
        log.info("====== SLA 超时检查开始 ======");

        List<Long> tenantIds = sysTenantMapper.selectEnabledTenantIds();
        log.info("共扫描 {} 个租户", tenantIds.size());

        for (Long tenantId : tenantIds) {
            TenantContext.setTenantId(tenantId);
            try {
                checkSlaForTenant(tenantId);
            } finally {
                TenantContext.clear();
            }
        }

        log.info("====== SLA 超时检查结束 ======");
    }

    private void checkSlaForTenant(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(30);

        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Ticket::getSlaDeadline, threshold)
                .ge(Ticket::getSlaDeadline, now.minusHours(1))
                .notIn(Ticket::getStatus, TicketStatus.CLOSED.name(), TicketStatus.RESOLVED.name());

        List<Ticket> tickets = ticketMapper.selectList(wrapper);
        log.info("租户 [{}] 发现 {} 个即将超时的工单", tenantId, tickets.size());

        for (Ticket ticket : tickets) {
            boolean overdue = ticket.getSlaDeadline().isBefore(now);
            String operation = overdue ? "SLA_OVERDUE" : "SLA_WARNING";

            log.warn("工单 [{}] {} - SLA到期时间: {}, 当前状态: {}",
                    ticket.getId(), overdue ? "已超时" : "即将超时",
                    ticket.getSlaDeadline(), ticket.getStatus());

            TicketNotifyMessage msg = new TicketNotifyMessage(
                    ticket.getId(), ticket.getTitle(), operation,
                    null, null,
                    null, ticket.getCreatorId()
            );
            messageProducer.sendTicketStatusChangedNotify(msg);
        }
    }
}
