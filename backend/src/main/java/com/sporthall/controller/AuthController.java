package com.sporthall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sporthall.common.Result;
import com.sporthall.dto.LoginDTO;
import com.sporthall.dto.RegisterDTO;
import com.sporthall.entity.User;
import com.sporthall.mapper.UserMapper;
import com.sporthall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!dto.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        String role = user.getRoleId() == 1 ? "admin" : "user";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", role);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        User exist = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (exist != null) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setRoleId(2L);
        userMapper.insert(user);
        return Result.success();
    }

    @PostMapping("/admin/login")
    public Result<?> adminLogin(@RequestBody LoginDTO dto) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null || user.getRoleId() != 1) {
            return Result.error("管理员账号不存在");
        }
        if (!dto.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "admin");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", "admin");
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return Result.success(data);
    }
}
