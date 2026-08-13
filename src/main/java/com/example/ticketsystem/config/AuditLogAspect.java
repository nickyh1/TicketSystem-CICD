package com.example.ticketsystem.config;

import com.example.ticketsystem.common.Log;
import com.example.ticketsystem.entity.SysAuditLog;
import com.example.ticketsystem.mapper.SysAuditLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint point, Log logAnnotation) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        Long userId = null;
        String ip = null;
        if (request != null) {
            userId = (Long) request.getAttribute("userId");
            ip = request.getRemoteAddr();
        }

        // 获取入参作为 new_value
        String newValue = null;
        if (point.getArgs().length > 0) {
            try {
                newValue = objectMapper.writeValueAsString(point.getArgs()[0]);
            } catch (Exception e) {
                newValue = point.getArgs()[0].toString();
            }
        }

        // 执行原方法
        Object result = point.proceed();

        // 记录审计日志
        try {
            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setUserId(userId);
            auditLog.setOperation(logAnnotation.value());
            auditLog.setTargetType(logAnnotation.targetType());
            auditLog.setNewValue(newValue);
            auditLog.setIp(ip);

            // 从返回值提取 target_id
            if (result != null) {
                String resultJson = objectMapper.writeValueAsString(result);
                // 简单提取：如果返回的是 Result<TicketVO>，取 data.id
                var node = objectMapper.readTree(resultJson);
                if (node.has("data") && node.get("data").has("id")) {
                    auditLog.setTargetId(node.get("data").get("id").asLong());
                }
            }

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // 审计日志记录失败不影响主业务
            log.error("审计日志记录失败", e);
        }

        return result;
    }
}