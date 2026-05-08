package com.sporthall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "TestSecretKeyForUnitTestingOnly123456789";
    private final Long expiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    @DisplayName("生成Token - 正常场景")
    void generateToken_success() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("解析Token - 获取用户ID")
    void getUserId_success() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        Long userId = jwtUtil.getUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("解析Token - 获取用户名")
    void getUsername_success() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        String username = jwtUtil.getUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("解析Token - 获取角色")
    void getRole_success() {
        String token = jwtUtil.generateToken(1L, "testuser", "admin");
        String role = jwtUtil.getRole(token);
        assertEquals("admin", role);
    }

    @Test
    @DisplayName("Token未过期检测 - 正常返回false")
    void isTokenExpired_notExpired() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("解析已过期Token - 抛出ExpiredJwtException")
    void isTokenExpired_expired() {
        // Manually create an expired token
        String expiredToken = Jwts.builder()
                .claim("userId", 1L)
                .claim("username", "testuser")
                .claim("role", "user")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        // parseToken throws ExpiredJwtException for expired tokens
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtUtil.parseToken(expiredToken));
    }

    @Test
    @DisplayName("解析无效Token - 抛出异常")
    void parseToken_invalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid.token.here"));
    }

    @Test
    @DisplayName("解析篡改Token - 抛出异常")
    void parseToken_tamperedToken() {
        String validToken = jwtUtil.generateToken(1L, "testuser", "user");
        String tampered = validToken + "tampered";
        assertThrows(Exception.class, () -> jwtUtil.parseToken(tampered));
    }

    @Test
    @DisplayName("Token内容完整性 - 所有字段正确")
    void token_integrity() {
        String token = jwtUtil.generateToken(99L, "admin", "admin");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(99L, claims.get("userId", Long.class));
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("admin", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Token过期时间 - 正确设置")
    void token_expirationTime() {
        long before = System.currentTimeMillis();
        String token = jwtUtil.generateToken(1L, "user", "user");
        long after = System.currentTimeMillis();

        Claims claims = jwtUtil.parseToken(token);
        long expectedExpiration = before + expiration;
        // Allow some tolerance for test execution time
        assertTrue(claims.getExpiration().getTime() >= expectedExpiration - 1000);
        assertTrue(claims.getExpiration().getTime() <= after + expiration + 1000);
    }
}
