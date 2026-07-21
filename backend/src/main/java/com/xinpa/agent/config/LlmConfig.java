package com.xinpa.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {
    private String apiBaseUrl = "http://localhost:11434/v1";
    private String apiKey = "";
    private String model = "qwen2.5:7b";
}
