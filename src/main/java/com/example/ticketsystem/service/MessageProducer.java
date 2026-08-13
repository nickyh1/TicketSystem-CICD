package com.example.ticketsystem.service;

import com.example.ticketsystem.config.RabbitMQConfig;
import com.example.ticketsystem.dto.CacheDeleteMessage;
import com.example.ticketsystem.dto.TicketNotifyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendTicketCreatedNotify(TicketNotifyMessage message) {
        log.info("发送工单创建通知: ticketId={}", message.getTicketId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_EXCHANGE,
                RabbitMQConfig.TICKET_CREATED_KEY, message);
    }

    public void sendTicketStatusChangedNotify(TicketNotifyMessage message) {
        log.info("发送工单状态变更通知: ticketId={}, {} -> {}",
                message.getTicketId(), message.getOldStatus(), message.getNewStatus());
        rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_EXCHANGE,
                RabbitMQConfig.TICKET_STATUS_CHANGED_KEY, message);
    }

    public void sendCacheDeleteMessage(CacheDeleteMessage message) {
        log.info("发送缓存删除消息: key={}", message.getCacheKey());
        rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_EXCHANGE,
                RabbitMQConfig.CACHE_DELETE_KEY, message);
    }
}
