package com.sporthall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.common.Result;
import com.sporthall.entity.Order;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long venueId = Long.valueOf(body.get("venueId").toString());
        LocalDate bookDate = LocalDate.parse(body.get("bookDate").toString());
        LocalTime startTime = LocalTime.parse(body.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(body.get("endTime").toString());
        Order order = orderService.create(JwtInterceptor.USER_ID.get(), venueId, bookDate, startTime, endTime);
        return Result.success(order);
    }

    @GetMapping
    public Result<?> listByUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.listByUser(JwtInterceptor.USER_ID.get(), page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id, JwtInterceptor.USER_ID.get(), JwtInterceptor.USER_ROLE.get()));
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        orderService.cancel(id, JwtInterceptor.USER_ID.get());
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        orderService.pay(id, JwtInterceptor.USER_ID.get());
        return Result.success();
    }

}
