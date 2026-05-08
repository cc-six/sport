package com.sporthall.controller;

import com.sporthall.common.Result;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<?> listAll(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        JwtInterceptor.requireAdmin();
        return Result.success(orderService.listAll(status, page, pageSize));
    }
}
