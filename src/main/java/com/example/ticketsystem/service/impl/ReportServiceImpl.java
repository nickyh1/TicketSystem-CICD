package com.example.ticketsystem.service.impl;

import com.example.ticketsystem.dto.TicketReportVO;
import com.example.ticketsystem.mapper.TicketMapper;
import com.example.ticketsystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TicketMapper ticketMapper;

    @Override
    public TicketReportVO getTicketReport(Long tenantId) {
        TicketReportVO report = new TicketReportVO();

        // 各状态数量
        Map<String, Long> statusCount = new HashMap<>();
        List<Map<String, Object>> statusRows = ticketMapper.countByStatus(tenantId);
        for (Map<String, Object> row : statusRows) {
            statusCount.put(row.get("status").toString(), ((Number) row.get("cnt")).longValue());
        }
        report.setStatusCount(statusCount);

        // 各优先级数量
        Map<String, Long> priorityCount = new HashMap<>();
        List<Map<String, Object>> priorityRows = ticketMapper.countByPriority(tenantId);
        for (Map<String, Object> row : priorityRows) {
            priorityCount.put(row.get("priority").toString(), ((Number) row.get("cnt")).longValue());
        }
        report.setPriorityCount(priorityCount);

        // 平均处理时长
        report.setAvgProcessingHours(ticketMapper.avgProcessingHours(tenantId));

        // 总数
        long total = statusCount.values().stream().mapToLong(Long::longValue).sum();
        report.setTotalTickets(total);

        return report;
    }
}
