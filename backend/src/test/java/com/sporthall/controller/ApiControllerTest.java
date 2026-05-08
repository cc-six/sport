package com.sporthall.controller;

import com.sporthall.service.OrderService;
import com.sporthall.service.VenueService;
import com.sporthall.service.EquipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API 集成测试 (H2)")
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ============ Auth Tests ============

    @Test
    @DisplayName("用户登录 - 成功")
    void login_success() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void login_wrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void login_userNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"pass123\",\"phone\":\"13900000001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("管理员登录 - 成功")
    void adminLogin_success() throws Exception {
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("admin"));
    }

    // ============ Venue Tests ============

    @Test
    @DisplayName("获取场地列表 - 分页")
    void venues_list() throws Exception {
        // Login first to get token
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/venues")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("获取场地列表 - 类型筛选")
    void venues_list_filterByType() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/venues")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("type", "badminton")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取场地详情")
    void venues_detail() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/venues/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("获取场地排期")
    void venues_schedule() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/venues/1/schedule")
                        .param("date", "2026-05-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());
    }

    // ============ Order Tests (need token) ============

    @Test
    @DisplayName("创建订单 - 成功")
    void order_create() throws Exception {
        String token = loginAndGetToken("testuser", "user123");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"venueId\":1,\"bookDate\":\"2026-06-01\",\"startTime\":\"08:00\",\"endTime\":\"09:00\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty());
    }

    @Test
    @DisplayName("创建订单 - 时段冲突")
    void order_create_conflict() throws Exception {
        String token = loginAndGetToken("testuser", "user123");

        // First create an order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"venueId\":2,\"bookDate\":\"2026-07-01\",\"startTime\":\"09:00\",\"endTime\":\"10:00\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Try to create overlapping order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"venueId\":2,\"bookDate\":\"2026-07-01\",\"startTime\":\"09:30\",\"endTime\":\"10:30\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ============ Equipment Tests ============

    @Test
    @DisplayName("获取器材列表")
    void equipments_list() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/equipments")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records", hasSize(greaterThan(0))));
    }

    // ============ Helper Methods ============

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // Parse token from JSON response: {"code":200,"message":"...","data":{"token":"xxx",...}}
        com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        return node.get("data").get("token").asText();
    }
}
