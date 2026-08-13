package com.example.ticketsystem.config;

import com.example.ticketsystem.dto.CacheDeleteMessage;
import com.example.ticketsystem.mapper.SysPermissionMapper;
import com.example.ticketsystem.mapper.SysRoleMapper;
import com.example.ticketsystem.service.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final StringRedisTemplate redisTemplate;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    private final MessageProducer messageProducer;

    private static final String ROLE_KEY_PREFIX = "user:roles:";
    private static final String KEY_PREFIX = "user:permissions:";
    private static final long EXPIRE_HOURS = 2;

    /**
     * 获取用户角色列表
     */
    public List<String> getRoles(Long userId) {
        String key = ROLE_KEY_PREFIX + userId;

        List<String> cached = redisTemplate.opsForList().range(key, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        List<String> roles = roleMapper.selectRoleKeysByUserId(userId);
        if (!roles.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, roles);
            redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return roles;
    }

    /**
     * 清除用户所有缓存（权限修改后调用）
     */
    public void evictAllCache(Long userId) {
        String permKey = KEY_PREFIX + userId;
        String roleKey = ROLE_KEY_PREFIX + userId;

        try {
            redisTemplate.delete(permKey);
            redisTemplate.delete(roleKey);
        } catch (Exception e) {
            // Redis 删除失败，发 MQ 补偿
            messageProducer.sendCacheDeleteMessage(new CacheDeleteMessage(permKey, 0));
            messageProducer.sendCacheDeleteMessage(new CacheDeleteMessage(roleKey, 0));
        }
    }

    /**
     * 获取用户权限（优先 Redis，miss 则查库并回填）
     */
    public List<String> getPermissions(Long userId) {
        String key = KEY_PREFIX + userId;

        // 先查 Redis
        List<String> cached = redisTemplate.opsForList().range(key, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }


        // Redis 没有，查数据库
        List<String> permissions = permissionMapper.selectPermKeysByUserId(userId);
        if (!permissions.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, permissions);
            redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return permissions;
    }

    /**
     * 清除用户权限缓存（管理员修改权限后调用）
     */
    public void evictCache(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
