package com.xinpa.security;

import com.xinpa.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类 — 双 Token 机制
 * <p>
 * accessToken:  有效期 2 小时，用于接口认证
 * refreshToken: 有效期 7 天，用于换取新的 accessToken，存入 Redis 可主动吊销
 */
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final StringRedisTemplate stringRedisTemplate;

    /** accessToken 过期时间：2 小时 */
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L;
    /** refreshToken 过期时间：7 天 */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    /** Redis 中 refreshToken 的 key 前缀 */
    private static final String REFRESH_KEY_PREFIX = "refresh_token:";

    public JwtUtils(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成 accessToken（2 小时有效）
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("tokenType", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 refreshToken（7 天有效），并存入 Redis
     */
    public String generateRefreshToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("tokenType", "refresh");

        String token = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();

        // 存入 Redis，旧的 refreshToken 自动覆盖（实现单设备登录 / 踢下线）
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + userId,
                token,
                REFRESH_TOKEN_EXPIRATION,
                TimeUnit.MILLISECONDS);

        return token;
    }

    /**
     * 校验 refreshToken 是否有效
     *
     * @return 如果有效返回 Claims，否则返回 null
     */
    public Claims validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 必须是 refresh 类型
            if (!"refresh".equals(claims.get("tokenType"))) {
                return null;
            }
            // 检查过期
            if (claims.getExpiration().before(new Date())) {
                return null;
            }
            // 检查 Redis 中是否匹配（用于主动吊销）
            Long userId = claims.get("userId", Long.class);
            String stored = stringRedisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + userId);
            if (stored == null || !stored.equals(token)) {
                return null;
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 吊销用户的 refreshToken（踢下线）
     */
    public void revokeRefreshToken(Long userId) {
        stringRedisTemplate.delete(REFRESH_KEY_PREFIX + userId);
    }

    /**
     * 解析 token（不校验类型和 Redis）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查 accessToken 是否有效（仅解析校验，不含 Redis）
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
