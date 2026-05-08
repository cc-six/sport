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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VenueService 单元测试")
class VenueServiceTest {

    @Mock
    private VenueMapper venueMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private VenueService venueService;

    private Venue venue1;
    private Venue venue2;

    @BeforeEach
    void setUp() {
        venue1 = new Venue();
        venue1.setId(1L);
        venue1.setName("羽毛球场地1");
        venue1.setType("badminton");
        venue1.setStatus(1);
        venue1.setOpenTime(LocalTime.of(8, 0));
        venue1.setCloseTime(LocalTime.of(22, 0));
        venue1.setPricePerHour(new BigDecimal("50.00"));

        venue2 = new Venue();
        venue2.setId(2L);
        venue2.setName("篮球馆");
        venue2.setType("basketball");
        venue2.setStatus(1);
        venue2.setOpenTime(LocalTime.of(9, 0));
        venue2.setCloseTime(LocalTime.of(21, 0));
        venue2.setPricePerHour(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("场地列表 - 分页查询")
    void list_pagination() {
        Page<Venue> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(venue1, venue2));
        when(venueMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<Venue> result = venueService.list(1, 10, null);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
    }

    @Test
    @DisplayName("场地列表 - 按类型筛选")
    void list_filterByType() {
        Page<Venue> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(venue1));
        when(venueMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<Venue> result = venueService.list(1, 10, "badminton");

        assertNotNull(result);
        verify(venueMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("场地列表 - 仅返回启用的场地")
    void list_onlyActiveVenues() {
        Page<Venue> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(venue1));
        when(venueMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        venueService.list(1, 10, null);

        // Verify the wrapper includes status=1 filter
        verify(venueMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("获取所有场地 - 不分页")
    void listAll_success() {
        when(venueMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(venue1, venue2));

        List<Venue> result = venueService.listAll();

        assertEquals(2, result.size());
        verify(venueMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取场地")
    void getById_success() {
        when(venueMapper.selectById(1L)).thenReturn(venue1);

        Venue result = venueService.getById(1L);

        assertNotNull(result);
        assertEquals("羽毛球场地1", result.getName());
    }

    @Test
    @DisplayName("添加场地")
    void add_success() {
        venueService.add(venue1);

        verify(venueMapper).insert(venue1);
    }

    @Test
    @DisplayName("更新场地")
    void update_success() {
        venueService.update(venue1);

        verify(venueMapper).updateById(venue1);
    }

    @Test
    @DisplayName("更新场地状态")
    void updateStatus_success() {
        venueService.updateStatus(1L, 0);

        verify(venueMapper).updateById(argThat(venue ->
                venue.getId().equals(1L) && venue.getStatus().equals(0)
        ));
    }

    @Test
    @DisplayName("获取场地排期 - 无预约")
    void getSchedule_noBookings() {
        when(venueMapper.selectById(1L)).thenReturn(venue1);
        when(orderMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList());

        Map<String, Boolean> schedule = venueService.getSchedule(1L, LocalDate.of(2026, 5, 1));

        // Venue open 08:00 to 22:00, so 14 slots
        assertEquals(14, schedule.size());
        // All slots should be unbooked
        schedule.values().forEach(booked -> assertFalse(booked));
        // Verify first and last slot keys
        assertTrue(schedule.containsKey("08:00"));
        assertTrue(schedule.containsKey("21:00"));
    }

    @Test
    @DisplayName("获取场地排期 - 有预约")
    void getSchedule_withBookings() {
        when(venueMapper.selectById(1L)).thenReturn(venue1);

        // Existing booking: 10:00-12:00
        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setStartTime(LocalTime.of(10, 0));
        existingOrder.setEndTime(LocalTime.of(12, 0));
        existingOrder.setStatus(1);

        when(orderMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(existingOrder));

        Map<String, Boolean> schedule = venueService.getSchedule(1L, LocalDate.of(2026, 5, 1));

        assertTrue(schedule.get("10:00"));
        assertTrue(schedule.get("11:00"));
        assertFalse(schedule.get("08:00"));
        assertFalse(schedule.get("09:00"));
        assertFalse(schedule.get("12:00"));
    }

    @Test
    @DisplayName("获取场地排期 - 多个重叠预约")
    void getSchedule_multipleBookings() {
        when(venueMapper.selectById(1L)).thenReturn(venue1);

        Order order1 = new Order();
        order1.setStartTime(LocalTime.of(8, 0));
        order1.setEndTime(LocalTime.of(10, 0));
        order1.setStatus(1);

        Order order2 = new Order();
        order2.setStartTime(LocalTime.of(14, 0));
        order2.setEndTime(LocalTime.of(16, 0));
        order2.setStatus(0);

        when(orderMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(order1, order2));

        Map<String, Boolean> schedule = venueService.getSchedule(1L, LocalDate.of(2026, 5, 1));

        assertTrue(schedule.get("08:00"));
        assertTrue(schedule.get("09:00"));
        assertTrue(schedule.get("14:00"));
        assertTrue(schedule.get("15:00"));
        assertFalse(schedule.get("10:00"));
        assertFalse(schedule.get("12:00"));
    }
}
