package com.xinpa.security;

import com.xinpa.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        // 使用固定密钥（至少256位 = 32字符）
        props.setSecret("ThisIsATestSecretKeyForJWTHmacSha256!");
        props.setExpiration(3600000L); // 1小时
        jwtUtils = new JwtUtils(props);
    }

    @Test
    @DisplayName("生成和解析Token")
    void generateAndParse() {
        String token = jwtUtils.generateToken(100L, "testuser", "USER");
        assertNotNull(token);

        Claims claims = jwtUtils.parseToken(token);
        assertEquals("testuser", claims.getSubject());
        assertEquals(100L, claims.get("userId", Long.class).longValue());
        assertEquals("USER", claims.get("userType", String.class));
    }

    @Test
    @DisplayName("有效Token验证")
    void validToken() {
        String token = jwtUtils.generateToken(100L, "testuser", "USER");
        assertTrue(jwtUtils.isTokenValid(token));
    }

    @Test
    @DisplayName("无效Token验证")
    void invalidToken() {
        assertFalse(jwtUtils.isTokenValid("invalid_token_here"));
    }

    @Test
    @DisplayName("不同用户生成不同Token")
    void differentUsers() {
        String token1 = jwtUtils.generateToken(100L, "user1", "USER");
        String token2 = jwtUtils.generateToken(200L, "user2", "ADMIN");

        Claims c1 = jwtUtils.parseToken(token1);
        Claims c2 = jwtUtils.parseToken(token2);

        assertEquals(100L, c1.get("userId", Long.class).longValue());
        assertEquals(200L, c2.get("userId", Long.class).longValue());
        assertEquals("USER", c1.get("userType"));
        assertEquals("ADMIN", c2.get("userType"));
    }
}
