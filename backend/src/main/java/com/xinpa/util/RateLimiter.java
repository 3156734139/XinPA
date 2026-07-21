package com.xinpa.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 限流器 — 滑动窗口计数器
 */
@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试放行
     *
     * @param key    限流 key（如 "login:192.168.1.1"）
     * @param max    窗口内最大次数
     * @param window 窗口长度（秒）
     * @return true=放行，false=限流
     */
    public boolean tryAcquire(String key, int max, long window) {
        String redisKey = "ratelimit:" + key;
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count == null) return true;
        if (count == 1) {
            stringRedisTemplate.expire(redisKey, window, TimeUnit.SECONDS);
        }
        return count <= max;
    }

    /**
     * 获取当前计数
     */
    public long getCount(String key) {
        String val = stringRedisTemplate.opsForValue().get("ratelimit:" + key);
        return val == null ? 0 : Long.parseLong(val);
    }

    /**
     * 重置计数
     */
    public void reset(String key) {
        stringRedisTemplate.delete("ratelimit:" + key);
    }
}
