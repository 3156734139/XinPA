package com.xinpa.service;

/**
 * 短信验证码服务
 */
public interface SmsCodeService {

    /**
     * 生成并发送验证码
     *
     * @param phone 手机号
     */
    void sendCode(String phone);

    /**
     * 校验验证码（校验后立即失效，一次性使用）
     *
     * @param phone 手机号
     * @param code  验证码
     * @return true 校验通过
     */
    boolean verifyCode(String phone, String code);
}
