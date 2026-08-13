package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.TicketReportVO;

public interface ReportService {
    TicketReportVO getTicketReport(Long tenantId);
}
