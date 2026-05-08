package com.sporthall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.common.Result;
import com.sporthall.entity.Venue;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type) {
        return Result.success(venueService.list(page, pageSize, type));
    }

    @GetMapping("/all")
    public Result<?> listAll() {
        JwtInterceptor.requireAdmin();
        return Result.success(venueService.listAll());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(venueService.getById(id));
    }

    @PostMapping
    public Result<?> add(@RequestBody Venue venue) {
        JwtInterceptor.requireAdmin();
        venueService.add(venue);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Venue venue) {
        JwtInterceptor.requireAdmin();
        venue.setId(id);
        venueService.update(venue);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        JwtInterceptor.requireAdmin();
        venueService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/{id}/schedule")
    public Result<?> getSchedule(@PathVariable Long id, @RequestParam String date) {
        return Result.success(venueService.getSchedule(id, LocalDate.parse(date)));
    }
}
