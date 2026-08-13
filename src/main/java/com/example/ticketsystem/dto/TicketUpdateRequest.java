package com.example.ticketsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketUpdateRequest {

    @Size(max = 200, message = "标题最长200字")
    private String title;

    private String description;

    private String priority;

    private Long assigneeId;
}
