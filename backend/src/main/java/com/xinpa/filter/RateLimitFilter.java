package com.xinpa.filter;

import com.xinpa.util.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 限流过滤器 —— 在 Spring Security 链最前端执行
 * <p>
 * 限制规则：
 * /auth/login, /admin/auth/login              → 10次/5分钟（IP）
 * /auth/register                              → 5次/30分钟（IP）
 * /auth/send-code                             → 3次/5分钟（IP）
 * /materials (GET)  /materials/{id}/video-url  → 60次/分钟（IP）
 * 其他认证接口兜底                            → 120次/分钟（IP）
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiPath = path.substring(4);
        String ip = getClientIp(request);
        RateLimitRule rule = resolveRule(apiPath, method);

        if (rule != null) {
            String key = rule.keyPrefix + ":" + ip;
            if (!rateLimiter.tryAcquire(key, rule.max, rule.windowSeconds)) {
                log.warn("限流触发: path={}, ip={}, rule={}", apiPath, ip, rule.keyPrefix);
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求太频繁，请稍后再试\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitRule resolveRule(String path, String method) {
        if (path.equals("/auth/login") || path.equals("/admin/auth/login"))
            return new RateLimitRule("login", 10, 300);
        if (path.equals("/auth/register"))
            return new RateLimitRule("register", 5, 1800);
        if (path.equals("/auth/send-code"))
            return new RateLimitRule("sms", 3, 300);
        if (path.equals("/materials") && "GET".equalsIgnoreCase(method))
            return new RateLimitRule("materials", 60, 60);
        if (path.matches("/materials/\\d+/video-url") && "GET".equalsIgnoreCase(method))
            return new RateLimitRule("video", 30, 60);
        if (!path.startsWith("/admin/"))
            return new RateLimitRule("default", 120, 60);
        return null;
    }

    private static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
            return ip.split(",")[0].trim();
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
            return ip;
        return req.getRemoteAddr();
    }

    private record RateLimitRule(String keyPrefix, int max, long windowSeconds) {
    }
}
