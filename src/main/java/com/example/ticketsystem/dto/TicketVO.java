package com.example.ticketsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketVO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long creatorId;
    private Long assigneeId;
    private LocalDateTime slaDeadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
