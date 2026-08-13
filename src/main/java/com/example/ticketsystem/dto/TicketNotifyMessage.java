package com.example.ticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketNotifyMessage {
    private Long ticketId;
    private String title;
    private String operation;     // CREATED / STATUS_CHANGED
    private String oldStatus;
    private String newStatus;
    private Long operatorId;      // 操作人
    private Long targetUserId;    // 通知谁
}
