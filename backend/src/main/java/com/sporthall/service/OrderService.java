package com.sporthall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.entity.Order;
import com.sporthall.entity.Venue;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.mapper.OrderMapper;
import com.sporthall.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final VenueMapper venueMapper;

    @Transactional
    public Order create(Long userId, Long venueId, LocalDate bookDate, LocalTime startTime, LocalTime endTime) {
        if (userId == null) throw new RuntimeException("未登录");
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) throw new RuntimeException("场地不存在");
        if (venue.getStatus() == null || venue.getStatus() != 1) throw new RuntimeException("场地已下架，暂不可预约");
        if (bookDate == null || bookDate.isBefore(LocalDate.now())) throw new RuntimeException("不能预约过去日期");
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new RuntimeException("预约时段不合法");
        }
        if (startTime.isBefore(venue.getOpenTime()) || endTime.isAfter(venue.getCloseTime())) {
            throw new RuntimeException("预约时段超出场地开放时间");
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0 || minutes % 60 != 0) {
            throw new RuntimeException("预约时段必须按整小时选择");
        }

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("venue_id", venueId)
               .eq("book_date", bookDate)
               .in("status", 0, 1)
               .lt("start_time", endTime)
               .gt("end_time", startTime);
        if (orderMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("时段已被预约，请选择其他时段");
        }

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setVenueId(venueId);
        order.setBookDate(bookDate);
        order.setStartTime(startTime);
        order.setEndTime(endTime);

        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        order.setTotalAmount(venue.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP));
        order.setStatus(0);
        orderMapper.insert(order);
        return order;
    }

    public Page<Order> listByUser(Long userId, int page, int pageSize) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public Page<Order> listAll(Integer status, int page, int pageSize) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (status != null) wrapper.eq("status", status);
        wrapper.orderByDesc("create_time");
        return orderMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public Order getById(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!"admin".equals(role) && !order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看该订单");
        }
        return order;
    }

    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    public void cancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态不允许取消");
        order.setStatus(2);
        orderMapper.updateById(order);
    }

    public void pay(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态不允许支付");
        order.setStatus(1);
        orderMapper.updateById(order);
    }
}
