package com.sporthall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.common.Result;
import com.sporthall.entity.Equipment;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/equipments")
    public Result<?> listEquipments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(equipmentService.list(page, pageSize));
    }

    @PostMapping("/equipments")
    public Result<?> addEquipment(@RequestBody Equipment equipment) {
        JwtInterceptor.requireAdmin();
        equipmentService.add(equipment);
        return Result.success();
    }

    @PutMapping("/equipments/{id}")
    public Result<?> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        JwtInterceptor.requireAdmin();
        equipment.setId(id);
        equipmentService.update(equipment);
        return Result.success();
    }

    @PostMapping("/rentals")
    public Result<?> rentEquipment(@RequestBody Map<String, Integer> body) {
        return Result.success(equipmentService.rent(
                JwtInterceptor.USER_ID.get(),
                body.get("equipmentId").longValue(),
                body.get("quantity")));
    }

    @GetMapping("/rentals")
    public Result<?> listMyRentals(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(equipmentService.listRentals(JwtInterceptor.USER_ID.get(), status, page, pageSize));
    }

    @GetMapping("/admin/rentals")
    public Result<?> listAllRentals(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        JwtInterceptor.requireAdmin();
        return Result.success(equipmentService.listRentals(null, status, page, pageSize));
    }

    @PutMapping("/rentals/{id}/return")
    public Result<?> returnEquipment(@PathVariable Long id) {
        JwtInterceptor.requireAdmin();
        equipmentService.returnRental(id);
        return Result.success();
    }
}
