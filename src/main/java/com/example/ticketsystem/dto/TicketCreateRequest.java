package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotBlank(message = "工单标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String title;

    private String description;

    private String priority;  // LOW / MEDIUM / HIGH / URGENT
}
