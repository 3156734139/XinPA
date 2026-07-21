package com.xinpa.filter;

import com.xinpa.util.ChineseIpChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * IP 过滤器 —— 只允许中国大陆 IP 访问，海外 IP 返回 403。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpFilter extends OncePerRequestFilter {

    private final ChineseIpChecker chineseIpChecker;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);

        if (!chineseIpChecker.isChineseIp(ip)) {
            log.warn("海外IP被拦截: {}", ip);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"只允许中国大陆 IP 访问\"}");
            return;
        }

        filterChain.doFilter(request, response);
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
}
