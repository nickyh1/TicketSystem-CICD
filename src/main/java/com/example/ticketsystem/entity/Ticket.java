package com.example.ticketsystem.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private String title;

    private String description;

    private String status;

    private String priority;

    private Long creatorId;

    private Long assigneeId;

    private LocalDateTime slaDeadline;

    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
