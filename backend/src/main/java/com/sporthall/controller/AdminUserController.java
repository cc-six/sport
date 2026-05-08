package com.sporthall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.common.Result;
import com.sporthall.entity.User;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/users")
    public Result<?> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        JwtInterceptor.requireAdmin();
        return Result.success(userMapper.selectPage(new Page<>(page, pageSize), null));
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        JwtInterceptor.requireAdmin();
        User user = new User();
        user.setId(id);
        user.setRoleId(body.get("roleId"));
        userMapper.updateById(user);
        return Result.success();
    }
}
