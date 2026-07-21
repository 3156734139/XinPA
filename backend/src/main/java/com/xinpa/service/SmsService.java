package com.xinpa.service;

/**
 * 短信发送服务
 */
public interface SmsService {

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     */
    void sendVerificationCode(String phone, String code);
}
