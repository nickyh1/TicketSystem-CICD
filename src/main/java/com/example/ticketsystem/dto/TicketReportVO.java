package com.example.ticketsystem.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TicketReportVO {
    private Map<String, Long> statusCount;       // 各状态数量
    private Map<String, Long> priorityCount;     // 各优先级数量
    private Double avgProcessingHours;           // 平均处理时长（小时）
    private Long totalTickets;                   // 工单总数
}
