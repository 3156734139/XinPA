package com.xinpa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 中国 IP 地址检测。
 * 调用 ip-api.com 查询 IP 归属地，结果缓存 1 小时。
 */
@Slf4j
@Component
public class ChineseIpChecker {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final long CACHE_TTL_MS = 3600_000; // 1 小时

    @PostConstruct
    public void init() {
        log.info("IP归属地检测：使用 ip-api.com");
    }

    /** 判断 IP 是否属于中国大陆 */
    public boolean isChineseIp(String ip) {
        if (ip == null || ip.isBlank()) return true;
        if (isPrivateIp(ip)) return true;

        // 查缓存
        CacheEntry cached = cache.get(ip);
        if (cached != null && System.currentTimeMillis() - cached.time < CACHE_TTL_MS) {
            return cached.isCn;
        }

        try {
            String url = "http://ip-api.com/json/" + ip + "?fields=countryCode,query";
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            boolean isCn = resp != null && "CN".equals(resp.get("countryCode"));

            cache.put(ip, new CacheEntry(isCn, System.currentTimeMillis()));

            if (!isCn) {
                log.warn("海外IP: {} -> countryCode={}", ip, resp != null ? resp.get("countryCode") : "unknown");
            }
            return isCn;
        } catch (Exception e) {
            log.warn("IP归属地查询失败: {} - {}", ip, e.getMessage());
            return true; // 查询失败不拦截
        }
    }

    private static boolean isPrivateIp(String ip) {
        if (ip == null) return false;
        // IPv6 loopback
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return true;
        try {
            if (ip.startsWith("127.") || ip.startsWith("10.")
                    || ip.startsWith("192.168.") || ip.startsWith("0.")
                    || ip.equals("localhost")) {
                return true;
            }
            if (ip.startsWith("172.")) {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private record CacheEntry(boolean isCn, long time) {}
}
