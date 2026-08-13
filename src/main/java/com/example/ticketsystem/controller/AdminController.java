package com.example.ticketsystem.controller;

import com.example.ticketsystem.common.RequiresPermission;
import com.example.ticketsystem.common.Result;
import com.example.ticketsystem.config.PermissionCacheService;
import com.example.ticketsystem.mapper.SysUserRoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员操作", description = "角色分配、权限管理")
public class AdminController {

    private final SysUserRoleMapper userRoleMapper;
    private final PermissionCacheService permissionCacheService;

    @PostMapping("/user/{userId}/role/{roleId}")
    @RequiresPermission("user:manage")
    @Operation(summary = "给用户分配角色")
    public Result<Void> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleMapper.insertUserRole(userId, roleId);
        // 清除该用户的权限缓存，下次请求会从数据库重新加载
        permissionCacheService.evictAllCache(userId);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}/role/{roleId}")
    @RequiresPermission("user:manage")
    @Operation(summary = "移除用户角色")
    public Result<Void> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleMapper.deleteUserRole(userId, roleId);
        permissionCacheService.evictAllCache(userId);
        return Result.success();
    }
}
