package com.example.ticketsystem.controller;

import com.example.ticketsystem.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("服务运行中");
    }
}
