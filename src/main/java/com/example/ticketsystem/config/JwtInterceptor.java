package com.example.ticketsystem.config;

import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.common.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录");
        }

        token = token.substring(7); // 去掉 "Bearer " 前缀

        try {
            Long userId = jwtUtils.getUserId(token);
            // 把 userId 放进 request，后续 Controller 可以通过 @RequestAttribute 取
            request.setAttribute("userId", userId);
            Long tenantId = jwtUtils.getTenantId(token);
            request.setAttribute("tenantId", tenantId);
            TenantContext.setTenantId(tenantId);
            List<String> permissions = jwtUtils.getPermissions(token);
            request.setAttribute("permissions", permissions);
            return true;
        } catch (Exception e) {
            throw new BusinessException(401, "token 无效或已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
