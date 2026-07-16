package com.xinpa.util;

import com.xinpa.common.BusinessException;
import com.xinpa.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI 大模型客户端（对接第三方 HTTP 接口）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用 AI 生成文案
     */
    public String generate(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey());

            Map<String, Object> body = Map.of(
                    "model", aiProperties.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 2048,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(aiProperties.getApiUrl(), request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            throw new BusinessException("AI 响应异常");
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException("AI 生成失败，请稍后重试");
        }
    }

    /**
     * 生成主页简介
     */
    public String generateIntro(String games, String templateType) {
        String prompt = String.format(
                "你是一个陪玩个人助理，请为以下陪玩生成一段吸引人的个人主页简介。" +
                "模板类型: %s，擅长游戏: %s。" +
                "要求: 语气亲切自然，突出个人特色，字数控制在150字以内。", templateType, games);
        return generate(prompt);
    }

    /**
     * 生成私聊开场白
     */
    public String generateOpeningLine(String nickname, String style) {
        String prompt = String.format(
                "请为陪玩【%s】生成一段私聊开场白。" +
                "风格: %s。" +
                "要求: 自然不尴尬，展现亲和力，20-50字。", nickname, style);
        return generate(prompt);
    }

    /**
     * 生成安抚话术
     */
    public String generateComfort(String customerName, String reason) {
        String prompt = String.format(
                "客户【%s】因【%s】产生负面情绪，请生成一段高情商安抚话术。" +
                "要求: 真诚不敷衍，语气温柔，30-80字。", customerName, reason);
        return generate(prompt);
    }

    /**
     * 生成砍价应对
     */
    public String generateBargainResponse(String packageName, String price) {
        String prompt = String.format(
                "客户对套餐【%s】（价格%s元）进行砍价，请生成一段得体的砍价应对话术。" +
                "要求: 既维护价格价值又不让客户反感，20-60字。", packageName, price);
        return generate(prompt);
    }

    /**
     * 生成活动方案
     */
    public String generateActivityPlan(String activityType) {
        String prompt = String.format(
                "请生成一份【%s】推广活动方案。" +
                "包含: 活动标题、优惠内容、话术建议。" +
                "要求: 简洁实用，吸引老客户回流。", activityType);
        return generate(prompt);
    }

    /**
     * 聊天风险识别（截图描述或对话文本）
     */
    public String riskDetection(String chatContent) {
        String prompt = String.format(
                "请分析以下聊天内容是否存在以下风险:\n" +
                "1. 白嫖风险（对方试图免费获取服务）\n" +
                "2. 安全隐患（危险线下邀约）\n" +
                "3. 恶意退款倾向\n\n" +
                "聊天内容:\n%s\n\n" +
                "请输出风险评估结果，格式: 风险等级(高/中/低)，风险类型，建议。", chatContent);
        return generate(prompt);
    }
}
