package com.example.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.service.TicketStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TicketStatsServiceImpl implements TicketStatsService {

    private final StringRedisTemplate redisTemplate;
    private final com.example.ticketsystem.mapper.TicketMapper ticketMapper;

    private static final String STATS_KEY_PREFIX = "ticket:stats:";

    @Override
    public Map<String, Integer> getStats() {
        Long tenantId = TenantContext.getTenantId();
        String key = STATS_KEY_PREFIX + tenantId;

        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);
        if (!cached.isEmpty()) {
            Map<String, Integer> result = new HashMap<>();
            cached.forEach((k, v) -> result.put(k.toString(), Integer.parseInt(v.toString())));
            return result;
        }

        return refreshAndReturn(tenantId);
    }

    @Override
    public void refreshCache() {
        Long tenantId = TenantContext.getTenantId();
        refreshAndReturn(tenantId);
    }

    private Map<String, Integer> refreshAndReturn(Long tenantId) {
        Map<String, Integer> stats = new HashMap<>();
        for (String status : new String[]{"PENDING", "PROCESSING", "RESOLVED", "CLOSED"}) {
            LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Ticket::getStatus, status);
            Long count = ticketMapper.selectCount(wrapper);
            stats.put(status, count.intValue());
        }

        String key = STATS_KEY_PREFIX + tenantId;
        Map<String, String> redisMap = new HashMap<>();
        stats.forEach((k, v) -> redisMap.put(k, v.toString()));
        redisTemplate.opsForHash().putAll(key, redisMap);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        return stats;
    }
}
