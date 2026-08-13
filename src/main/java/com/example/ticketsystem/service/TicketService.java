package com.example.ticketsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ticketsystem.dto.*;

import java.util.List;

public interface TicketService {
    TicketVO createTicket(TicketCreateRequest request, Long creatorId);
    TicketVO getTicketById(Long id);
    IPage<TicketVO> getMyTickets(Long userId, int page, int size);
    TicketVO updateTicket(Long id, TicketUpdateRequest request, Long userId);
    TicketVO transitStatus(Long id, String targetStatus, Long userId, List<String> roles);
    IPage<TicketVO> getTicketList(Long userId, List<String> roles, int page, int size);
    TicketVO getTicketById(Long id, Long userId, List<String> roles);

}
