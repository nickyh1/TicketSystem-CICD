package com.example.ticketsystem.service;

import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.dto.TicketCreateRequest;
import com.example.ticketsystem.dto.TicketVO;
import com.example.ticketsystem.entity.TicketStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.ticketsystem.common.BusinessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketServiceIT {

    @Autowired
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        // 模拟租户上下文
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createTicket_shouldSuccess() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("测试工单");
        request.setPriority("HIGH");

        TicketVO result = ticketService.createTicket(request, 1001L);

        assertNotNull(result.getId());
        assertEquals("测试工单", result.getTitle());
        assertEquals("HIGH", result.getPriority());
        assertEquals(TicketStatus.PENDING.name(), result.getStatus());
        assertEquals(1001L, result.getCreatorId());
    }
    @Test
    void receiveTicket_withoutPermission_shouldThrow() {
        // 先创建一个工单
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("权限测试工单");
        request.setPriority("MEDIUM");
        TicketVO ticket = ticketService.createTicket(request, 1001L);

        // 用一个没有权限的用户去查看（模拟数据权限校验）
        // user 9999 既不是创建人也不是处理人，也不是管理员
        List<String> noRoles = List.of();
        assertThrows(BusinessException.class, () -> {
            ticketService.getTicketById(ticket.getId(), 9999L, noRoles);
        });
    }

    @Test
    void transitStatus_nonOwnerWithoutAdminRole_shouldThrow() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("数据权限测试工单");
        request.setPriority("HIGH");
        TicketVO ticket = ticketService.createTicket(request, 1001L);

        // 9999 既不是创建人也不是处理人，也没有 admin 角色 → 403
        assertThrows(BusinessException.class, () ->
                ticketService.transitStatus(ticket.getId(), "PROCESSING", 9999L, List.of("handler"))
        );
    }

    @Test
    void transitStatus_byCreator_shouldSucceed() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("创建人流转测试");
        request.setPriority("MEDIUM");
        TicketVO ticket = ticketService.createTicket(request, 1001L);

        // 创建人 1001 自己推进状态
        TicketVO processing = ticketService.transitStatus(ticket.getId(), "PROCESSING", 1001L, List.of("handler"));
        assertEquals("PROCESSING", processing.getStatus());
    }

    @Test
    void ticketStatusFlow_shouldFollowRules() {
        // 创建工单
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("状态流转测试");
        request.setPriority("HIGH");
        TicketVO ticket = ticketService.createTicket(request, 1001L);

        // PENDING → PROCESSING（合法）
        TicketVO processing = ticketService.transitStatus(ticket.getId(), "PROCESSING", 2001L, List.of("admin"));
        assertEquals("PROCESSING", processing.getStatus());

        // PROCESSING → CLOSED（非法，应该报错）
        assertThrows(BusinessException.class, () -> {
            ticketService.transitStatus(ticket.getId(), "CLOSED", 2001L, List.of("admin"));
        });

        // PROCESSING → RESOLVED（合法）
        TicketVO resolved = ticketService.transitStatus(ticket.getId(), "RESOLVED", 2001L, List.of("admin"));
        assertEquals("RESOLVED", resolved.getStatus());

        // RESOLVED → CLOSED（合法）
        TicketVO closed = ticketService.transitStatus(ticket.getId(), "CLOSED", 2001L, List.of("admin"));
        assertEquals("CLOSED", closed.getStatus());

        // CLOSED → PENDING（非法，终态不可变）
        assertThrows(BusinessException.class, () -> {
            ticketService.transitStatus(ticket.getId(), "PENDING", 2001L, List.of("admin"));
        });
    }
}
