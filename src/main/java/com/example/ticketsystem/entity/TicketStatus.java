package com.example.ticketsystem.entity;

import java.util.List;
import java.util.Map;

public enum TicketStatus {
    PENDING,      // 待受理
    PROCESSING,   // 处理中
    RESOLVED,     // 已解决
    CLOSED;       // 已关闭

    // 定义允许的状态转换
    private static final Map<TicketStatus, List<TicketStatus>> TRANSITIONS = Map.of(
            PENDING,    List.of(PROCESSING),              // 待受理 → 处理中（接单）
            PROCESSING, List.of(RESOLVED, PENDING),       // 处理中 → 已解决（解决）/ 待受理（退回）
            RESOLVED,   List.of(CLOSED),                  // 已解决 → 已关闭（关闭）
            CLOSED,     List.of()                         // 已关闭 → 不能再变
    );

    /**
     * 判断是否可以从当前状态转换到目标状态
     */
    public boolean canTransitionTo(TicketStatus target) {
        return TRANSITIONS.getOrDefault(this, List.of()).contains(target);
    }
}
