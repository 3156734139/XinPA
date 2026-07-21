package com.xinpa.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.aliyun.teaopenapi.models.Config;
import com.xinpa.service.SmsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 阿里云号码认证-短信认证服务实现
 * 未配置 access-key-id 时自动降级为控制台输出（开发模式）
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    private Client client;
    private boolean aliyunEnabled;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
            try {
                Config config = new Config()
                        .setAccessKeyId(accessKeyId)
                        .setAccessKeySecret(accessKeySecret);
                config.endpoint = "dypnsapi.aliyuncs.com";
                client = new Client(config);
                aliyunEnabled = true;
                log.info("阿里云号码认证-短信认证服务初始化成功");
            } catch (Exception e) {
                log.warn("阿里云号码认证服务初始化失败，降级为控制台输出模式: {}", e.getMessage());
            }
        } else {
            log.info("阿里云号码认证未配置（access-key-id 为空），使用控制台输出模式");
        }
    }

    @Override
    public void sendVerificationCode(String phone, String code) {
        if (aliyunEnabled && client != null) {
            sendViaAliyun(phone, code);
        } else {
            log.info("[短信验证码] 手机号: {}, 验证码: {}", phone, code);
        }
    }

    private void sendViaAliyun(String phone, String code) {
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}")
                    .setCodeLength(6L);

            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse resp = client.sendSmsVerifyCodeWithOptions(request, runtime);
            String respCode = resp.getBody().getCode();
            String respMessage = resp.getBody().getMessage();

            if ("OK".equals(respCode)) {
                log.info("号码认证短信发送成功: phone={}", phone);
            } else {
                log.error("号码认证短信发送失败: phone={}, code={}, message={}", phone, respCode, respMessage);
                throw new RuntimeException("短信发送失败: " + respMessage);
            }
        } catch (com.aliyun.tea.TeaException e) {
            log.error("号码认证短信发送异常: phone={}, code={}, message={}", phone, e.code, e.message, e);
            throw new RuntimeException("短信发送失败: " + e.message);
        } catch (Exception e) {
            log.error("号码认证短信发送异常: phone={}", phone, e);
            throw new RuntimeException("短信发送异常", e);
        }
    }
}
