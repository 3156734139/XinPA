package com.xinpa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 接口配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String apiUrl;
    private String apiKey;
    private String model;
    private int dailyLimitFree;
    private int dailyLimitVip;
}
