package com.example.ticketsystem.service;

import com.example.ticketsystem.config.RabbitMQConfig;
import com.example.ticketsystem.dto.CacheDeleteMessage;
import com.example.ticketsystem.dto.TicketNotifyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final StringRedisTemplate redisTemplate;
    private final MessageProducer messageProducer;

    // 处理工单通知
    @RabbitListener(queues = RabbitMQConfig.TICKET_NOTIFY_QUEUE)
    public void handleTicketNotify(TicketNotifyMessage message) {
        log.info("====== 收到工单通知 ======");
        log.info("工单ID: {}", message.getTicketId());
        log.info("操作: {}", message.getOperation());
        log.info("通知用户: {}", message.getTargetUserId());

        if ("CREATED".equals(message.getOperation())) {
            // 模拟通知处理人：新工单待处理
            log.info("【模拟邮件】亲爱的处理人，有一个新工单「{}」等待处理", message.getTitle());
        } else if ("STATUS_CHANGED".equals(message.getOperation())) {
            // 模拟通知创建人：工单状态变更
            log.info("【模拟邮件】您的工单「{}」状态已从 {} 变更为 {}",
                    message.getTitle(), message.getOldStatus(), message.getNewStatus());
        }
    }

    // 处理缓存删除（重试机制）
    @RabbitListener(queues = RabbitMQConfig.CACHE_DELETE_QUEUE)
    public void handleCacheDelete(CacheDeleteMessage message) {
        log.info("收到缓存删除消息: key={}, retryCount={}", message.getCacheKey(), message.getRetryCount());
        try {
            Boolean deleted = redisTemplate.delete(message.getCacheKey());
            log.info("缓存删除{}: key={}", Boolean.TRUE.equals(deleted) ? "成功" : "不存在", message.getCacheKey());
        } catch (Exception e) {
            log.error("缓存删除失败: key={}", message.getCacheKey(), e);
            // 重试最多3次
            if (message.getRetryCount() < 3) {
                message.setRetryCount(message.getRetryCount() + 1);
                messageProducer.sendCacheDeleteMessage(message);
            } else {
                log.error("缓存删除重试次数用尽: key={}", message.getCacheKey());
            }
        }
    }

    // 处理死信队列（消费失败的消息）
    @RabbitListener(queues = RabbitMQConfig.TICKET_DEAD_QUEUE)
    public void handleDeadLetter(TicketNotifyMessage message) {
        log.error("====== 死信消息 ======");
        log.error("工单ID: {}, 操作: {}", message.getTicketId(), message.getOperation());
        // 实际项目中可以入库、发告警
    }
}
