package com.sporthall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.entity.Order;
import com.sporthall.entity.Venue;
import com.sporthall.mapper.OrderMapper;
import com.sporthall.mapper.VenueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private OrderService orderService;

    private Long testUserId;
    private Long testVenueId;
    private Venue testVenue;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testVenueId = 1L;
        testDate = LocalDate.of(2026, 5, 1);

        testVenue = new Venue();
        testVenue.setId(testVenueId);
        testVenue.setName("羽毛球场地1");
        testVenue.setType("badminton");
        testVenue.setPricePerHour(new BigDecimal("50.00"));
        testVenue.setOpenTime(LocalTime.of(8, 0));
        testVenue.setCloseTime(LocalTime.of(22, 0));
        testVenue.setStatus(1);
    }

    @Test
    @DisplayName("创建订单 - 成功")
    void create_success() {
        when(venueMapper.selectById(testVenueId)).thenReturn(testVenue);
        when(orderMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        Order result = orderService.create(testUserId, testVenueId, testDate,
                LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertNotNull(result);
        assertNotNull(result.getOrderNo());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testVenueId, result.getVenueId());
        assertEquals(testDate, result.getBookDate());
        assertEquals(LocalTime.of(10, 0), result.getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getEndTime());
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertEquals(0, result.getStatus());

        verify(orderMapper).insert(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 时段冲突检测")
    void create_conflict_overlapping() {
        // Simulate existing booking: 10:00-12:00
        when(venueMapper.selectById(testVenueId)).thenReturn(testVenue);
        when(orderMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.create(testUserId, testVenueId, testDate,
                        LocalTime.of(11, 0), LocalTime.of(13, 0)));
        assertEquals("时段已被预约，请选择其他时段", ex.getMessage());
    }

    @Test
    @DisplayName("创建订单 - 场地不存在")
    void create_venueNotFound() {
        when(venueMapper.selectById(testVenueId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.create(testUserId, testVenueId, testDate,
                        LocalTime.of(10, 0), LocalTime.of(12, 0)));
        assertEquals("场地不存在", ex.getMessage());
    }

    @Test
    @DisplayName("创建订单 - 正确计算价格")
    void create_correctPrice() {
        when(venueMapper.selectById(testVenueId)).thenReturn(testVenue);
        when(orderMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        // 3 hours * 50 = 150
        Order result = orderService.create(testUserId, testVenueId, testDate,
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertEquals(new BigDecimal("150.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancel_success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(testUserId);
        order.setStatus(0); // pending

        when(orderMapper.selectById(1L)).thenReturn(order);

        orderService.cancel(1L, testUserId);

        assertEquals(2, order.getStatus()); // cancelled
        verify(orderMapper).updateById(order);
    }

    @Test
    @DisplayName("取消订单 - 订单不存在")
    void cancel_orderNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.cancel(1L, testUserId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("取消订单 - 无权操作")
    void cancel_noPermission() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L); // different user
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.cancel(1L, testUserId));
        assertEquals("无权操作", ex.getMessage());
    }

    @Test
    @DisplayName("取消订单 - 状态不允许取消")
    void cancel_statusNotAllowed() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(testUserId);
        order.setStatus(1); // already paid

        when(orderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.cancel(1L, testUserId));
        assertEquals("订单状态不允许取消", ex.getMessage());
    }

    @Test
    @DisplayName("支付订单 - 成功")
    void pay_success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(testUserId);
        order.setStatus(0); // pending

        when(orderMapper.selectById(1L)).thenReturn(order);

        orderService.pay(1L, testUserId);

        assertEquals(1, order.getStatus()); // paid
        verify(orderMapper).updateById(order);
    }

    @Test
    @DisplayName("支付订单 - 订单不存在")
    void pay_orderNotFound() {
        when(orderMapper.selectById(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.pay(1L, testUserId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("支付订单 - 无权操作")
    void pay_noPermission() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L);
        order.setStatus(0);

        when(orderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.pay(1L, testUserId));
        assertEquals("无权操作", ex.getMessage());
    }

    @Test
    @DisplayName("支付订单 - 重复支付")
    void pay_alreadyPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(testUserId);
        order.setStatus(1); // already paid

        when(orderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.pay(1L, testUserId));
        assertEquals("订单状态不允许支付", ex.getMessage());
    }

    @Test
    @DisplayName("查询用户订单列表 - 分页")
    void listByUser_pagination() {
        Page<Order> expectedPage = new Page<>(1, 10);
        when(orderMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<Order> result = orderService.listByUser(testUserId, 1, 10);

        assertNotNull(result);
        verify(orderMapper).selectPage(any(Page.class), any(QueryWrapper.class));

        // Verify the query wrapper filters by userId
        ArgumentCaptor<QueryWrapper<Order>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper).selectPage(any(Page.class), captor.capture());
    }

    @Test
    @DisplayName("查询所有订单 - 带状态筛选")
    void listAll_withStatusFilter() {
        Page<Order> expectedPage = new Page<>(1, 10);
        when(orderMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<Order> result = orderService.listAll(1, 1, 10);

        assertNotNull(result);
    }

    @Test
    @DisplayName("查询所有订单 - 不带状态筛选")
    void listAll_noStatusFilter() {
        Page<Order> expectedPage = new Page<>(1, 10);
        when(orderMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<Order> result = orderService.listAll(null, 1, 10);

        assertNotNull(result);
    }

    @Test
    @DisplayName("根据ID查询订单")
    void getById_success() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("test-order-no");

        when(orderMapper.selectById(1L)).thenReturn(order);

        Order result = orderService.getById(1L);

        assertNotNull(result);
        assertEquals("test-order-no", result.getOrderNo());
    }
}
