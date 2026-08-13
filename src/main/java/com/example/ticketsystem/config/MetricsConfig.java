package com.example.ticketsystem.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter ticketCreatedCounter(MeterRegistry registry) {
        return Counter.builder("ticket.created.total")
                .description("Total number of tickets created")
                .tag("type", "ticket")
                .register(registry);
    }
}
