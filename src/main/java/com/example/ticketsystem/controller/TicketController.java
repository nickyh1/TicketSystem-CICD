package com.example.ticketsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ticketsystem.common.Log;
import com.example.ticketsystem.common.RequiresPermission;
import com.example.ticketsystem.common.Result;
import com.example.ticketsystem.config.PermissionCacheService;
import com.example.ticketsystem.dto.*;
import com.example.ticketsystem.service.TicketService;
import com.example.ticketsystem.service.TicketStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
@Tag(name = "工单管理", description = "工单的创建、查询、更新、状态流转")
public class TicketController {

    private final TicketService ticketService;
    private final PermissionCacheService permissionCacheService;
    private final TicketStatsService ticketStatsService;

    @GetMapping("/stats")
    @RequiresPermission("ticket:view")
    public Result<Map<String, Integer>> stats() {
        return Result.success(ticketStatsService.getStats());
    }

    @GetMapping("/{id}")
    @RequiresPermission("ticket:view")
    @Operation(summary = "查看工单详情")
    public Result<TicketVO> getById(@PathVariable Long id,
                                    @RequestAttribute("userId") Long userId) {
        List<String> roles = permissionCacheService.getRoles(userId);
        return Result.success(ticketService.getTicketById(id, userId, roles));
    }

    // 创建工单
    @PostMapping
    @RequiresPermission("ticket:create")
    @Log(value = "创建工单", targetType = "TICKET")
    @Operation(summary = "创建工单", description = "创建一个新工单，默认状态为PENDING")
    public Result<TicketVO> create(@Valid @RequestBody TicketCreateRequest request,
                                   @RequestAttribute("userId") Long userId) {
        return Result.success(ticketService.createTicket(request, userId));
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("ticket:process")
    @Log(value = "工单状态变更", targetType = "TICKET")
    public Result<TicketVO> transitStatus(@PathVariable Long id,
                                          @Valid @RequestBody TicketTransitionRequest request,
                                          @RequestAttribute("userId") Long userId) {
        List<String> roles = permissionCacheService.getRoles(userId);
        return Result.success(ticketService.transitStatus(id, request.getTargetStatus(), userId, roles));
    }

    // 查看我的工单列表（分页）
    @GetMapping("/list")
    @RequiresPermission("ticket:view")
    public Result<IPage<TicketVO>> listTickets(@RequestAttribute("userId") Long userId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        List<String> roles = permissionCacheService.getRoles(userId);
        return Result.success(ticketService.getTicketList(userId, roles, page, size));
    }

    // 更新工单
    @PutMapping("/{id}")
    @RequiresPermission("ticket:update")
    @Log(value = "更新工单", targetType = "TICKET")
    public Result<TicketVO> update(@PathVariable Long id,
                                   @Valid @RequestBody TicketUpdateRequest request,
                                   @RequestAttribute("userId") Long userId) {
        return Result.success(ticketService.updateTicket(id, request, userId));
    }
}
