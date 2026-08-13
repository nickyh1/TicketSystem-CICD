package com.example.ticketsystem.config;

import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.common.RequiresPermission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionCacheService permissionCacheService;

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(401, "无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        // 从 request 取 userId（JWT 拦截器放进去的）
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 从 Redis（或数据库）获取最新权限
        List<String> permissions = permissionCacheService.getPermissions(userId);

        String required = requiresPermission.value();
        if (!permissions.contains(required)) {
            throw new BusinessException(403, "缺少权限: " + required);
        }

        return point.proceed();
    }
}
