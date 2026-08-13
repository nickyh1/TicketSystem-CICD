package com.example.ticketsystem.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String TICKET_EXCHANGE = "ticket.exchange";

    // Queue
    public static final String TICKET_NOTIFY_QUEUE = "ticket.notify.queue";
    public static final String TICKET_DEAD_QUEUE = "ticket.dead.queue";
    public static final String CACHE_DELETE_QUEUE = "cache.delete.queue";

    // Routing Key
    public static final String TICKET_CREATED_KEY = "ticket.created";
    public static final String TICKET_STATUS_CHANGED_KEY = "ticket.status.changed";
    public static final String CACHE_DELETE_KEY = "cache.delete";

    // 主交换机
    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(TICKET_EXCHANGE);
    }

    // 通知队列（带死信配置）
    @Bean
    public Queue ticketNotifyQueue() {
        return QueueBuilder.durable(TICKET_NOTIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TICKET_DEAD_QUEUE)
                .build();
    }

    // 死信队列
    @Bean
    public Queue ticketDeadQueue() {
        return QueueBuilder.durable(TICKET_DEAD_QUEUE).build();
    }

    // 缓存删除队列
    @Bean
    public Queue cacheDeleteQueue() {
        return QueueBuilder.durable(CACHE_DELETE_QUEUE).build();
    }

    // 绑定：工单创建 → 通知队列
    @Bean
    public Binding bindCreated(Queue ticketNotifyQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(ticketNotifyQueue).to(ticketExchange).with(TICKET_CREATED_KEY);
    }

    // 绑定：状态变更 → 通知队列
    @Bean
    public Binding bindStatusChanged(Queue ticketNotifyQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(ticketNotifyQueue).to(ticketExchange).with(TICKET_STATUS_CHANGED_KEY);
    }

    // 绑定：缓存删除 → 缓存队列
    @Bean
    public Binding bindCacheDelete(Queue cacheDeleteQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(cacheDeleteQueue).to(ticketExchange).with(CACHE_DELETE_KEY);
    }

    // 用 JSON 序列化消息
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
