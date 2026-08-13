package com.example.ticketsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private String username;

    private String password;

    private String email;

    private String phone;

    private Integer status;

    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
