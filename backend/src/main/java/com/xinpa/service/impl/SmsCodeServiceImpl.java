package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.service.SmsCodeService;
import com.xinpa.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现
 * 验证码用 Redis 存储，5 分钟有效，60 秒内不可重复发送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String SEND_KEY_PREFIX = "sms:send:";
    private static final long CODE_TTL_MINUTES = 5;
    private static final long RESEND_INTERVAL_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;
    private final SmsService smsService;

    @Override
    public void sendCode(String phone) {
        // 检查是否可重新发送（防刷）
        String sendKey = SEND_KEY_PREFIX + phone;
        Boolean hasSent = stringRedisTemplate.hasKey(sendKey);
        if (Boolean.TRUE.equals(hasSent)) {
            long ttl = stringRedisTemplate.getExpire(sendKey);
            throw new BusinessException("请 " + ttl + " 秒后再获取验证码");
        }

        // 生成 6 位验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));

        // 保存验证码到 Redis（5 分钟有效）
        String codeKey = CODE_KEY_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 标记发送频率（60 秒内不可重复发送）
        stringRedisTemplate.opsForValue().set(sendKey, "1", RESEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送短信
        smsService.sendVerificationCode(phone, code);

        log.info("验证码已发送: phone={}, code={}", phone, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String codeKey = CODE_KEY_PREFIX + phone;
        String saved = stringRedisTemplate.opsForValue().get(codeKey);
        if (saved != null && saved.equals(code)) {
            // 验证通过，立即删除（一次性使用）
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(SEND_KEY_PREFIX + phone);
            return true;
        }
        return false;
    }
}
