package com.example.ticketsystem.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String value();          // 操作描述，如 "创建工单"
    String targetType();     // 目标类型，如 "TICKET"
}