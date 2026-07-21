package com.xinpa.agent.service;

import com.xinpa.agent.config.LlmConfig;
import com.xinpa.agent.dto.ChatMessage;
import com.xinpa.agent.dto.ChatRequest;
import com.xinpa.agent.dto.ChatResponse;
import com.xinpa.agent.dto.ToolDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final RestTemplate restTemplate;
    private final LlmConfig llmConfig;

    public ChatResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        String url = llmConfig.getApiBaseUrl() + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (llmConfig.getApiKey() != null && !llmConfig.getApiKey().isEmpty()) {
            headers.setBearerAuth(llmConfig.getApiKey());
        }

        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .model(llmConfig.getModel())
                .messages(messages)
                .stream(false);

        if (tools != null && !tools.isEmpty()) {
            builder.tools(tools);
        }

        HttpEntity<ChatRequest> request = new HttpEntity<>(builder.build(), headers);

        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                url, request, ChatResponse.class);

        return response.getBody();
    }
}
