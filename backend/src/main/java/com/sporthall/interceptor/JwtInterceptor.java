package com.sporthall.interceptor;

import com.sporthall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    public static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void requireAdmin() {
        if (!"admin".equals(USER_ROLE.get())) {
            throw new RuntimeException("无管理员权限");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        token = token.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new RuntimeException("Token已过期");
            }
            USER_ID.set(jwtUtil.getUserId(token));
            USER_ROLE.set(jwtUtil.getRole(token));
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Token无效");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        USER_ID.remove();
        USER_ROLE.remove();
    }
}
