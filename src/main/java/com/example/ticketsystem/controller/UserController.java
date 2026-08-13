package com.example.ticketsystem.controller;

import com.example.ticketsystem.common.Result;
import com.example.ticketsystem.config.PermissionCacheService;
import com.example.ticketsystem.dto.*;
import com.example.ticketsystem.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "注册、登录、查询用户信息")
public class UserController {

    private final SysUserService userService;
    private final PermissionCacheService permissionCacheService;

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(@RequestAttribute("userId") Long userId) {
        permissionCacheService.evictAllCache(userId);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "登录成功返回JWT token")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        return Result.success(token);
    }

    @GetMapping("/info")
    @Operation(summary = "查询当前用户信息")
    public Result<UserVO> getUserInfo(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }
}
