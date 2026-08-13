package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketTransitionRequest {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;  // PROCESSING / RESOLVED / CLOSED / PENDING
}
