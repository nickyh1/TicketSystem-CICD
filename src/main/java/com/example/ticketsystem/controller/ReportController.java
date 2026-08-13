package com.example.ticketsystem.controller;

import com.example.ticketsystem.common.RequiresPermission;
import com.example.ticketsystem.common.Result;
import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.dto.TicketReportVO;
import com.example.ticketsystem.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "报表统计", description = "工单统计报表")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/ticket")
    @RequiresPermission("ticket:view:all")
    public Result<TicketReportVO> ticketReport() {
        Long tenantId = TenantContext.getTenantId();
        return Result.success(reportService.getTicketReport(tenantId));
    }
}
