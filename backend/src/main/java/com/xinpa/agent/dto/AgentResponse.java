package com.xinpa.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String type;       // "chat" | "order_preview"
    private String role;       // "assistant"
    private String content;    // 文字回复
    private Map<String, Object> preview;     // 预确认数据
    private String confirmToken;             // 确认凭证

    public static AgentResponse chat(String content) {
        return AgentResponse.builder()
                .type("chat")
                .role("assistant")
                .content(content)
                .build();
    }

    public static AgentResponse orderPreview(Map<String, Object> previewData) {
        String token = previewData != null ? (String) previewData.get("confirmToken") : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> preview = previewData != null
                ? (Map<String, Object>) previewData.get("preview") : null;
        return AgentResponse.builder()
                .type("order_preview")
                .role("assistant")
                .content("为您生成了以下订单预览，请确认：")
                .confirmToken(token)
                .preview(preview)
                .build();
    }
}
