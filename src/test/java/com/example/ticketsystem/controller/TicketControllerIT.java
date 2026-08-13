package com.example.ticketsystem.controller;

import com.example.ticketsystem.config.JwtUtils;
import com.example.ticketsystem.config.PermissionCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller + JwtInterceptor + PermissionAspect 集成测试
 *
 * 走完整的 DispatcherServlet → 拦截器 → AOP → Controller 链路。
 */
@SpringBootTest
@ActiveProfiles("test")
class TicketControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionCacheService permissionCacheService;

    private MockMvc mockMvc;

    private String tokenWithTicketCreate;
    private String tokenWithNoPermissions;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // 用户 1001：持有 ticket:create + ticket:view
        tokenWithTicketCreate = jwtUtils.generateToken(
                1001L, "testuser",
                List.of("ticket:create", "ticket:view"),
                1L
        );

        // 用户 2001：无任何权限
        tokenWithNoPermissions = jwtUtils.generateToken(
                2001L, "nopermuser",
                List.of(),
                1L
        );

        when(permissionCacheService.getPermissions(1001L))
                .thenReturn(List.of("ticket:create", "ticket:view"));
        when(permissionCacheService.getPermissions(2001L))
                .thenReturn(List.of());
        when(permissionCacheService.getRoles(anyLong()))
                .thenReturn(List.of());
    }

    /**
     * 测试1：无 Authorization header → JwtInterceptor 拦截返回 401
     */
    @Test
    void request_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/ticket/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    /**
     * 测试2：token 有效但缺少 ticket:create 权限 → PermissionAspect 返回 403
     */
    @Test
    void createTicket_withoutPermission_returns403() throws Exception {
        Map<String, String> body = Map.of("title", "无权工单", "priority", "MEDIUM");

        mockMvc.perform(post("/api/ticket")
                        .header("Authorization", "Bearer " + tokenWithNoPermissions)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    /**
     * 测试3：有效 token + ticket:create 权限 → 创建工单成功，返回 200
     */
    @Test
    void createTicket_withValidTokenAndPermission_returns200() throws Exception {
        Map<String, String> body = Map.of("title", "集成测试工单", "priority", "HIGH");

        mockMvc.perform(post("/api/ticket")
                        .header("Authorization", "Bearer " + tokenWithTicketCreate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("集成测试工单"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
