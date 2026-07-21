package com.xinpa.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private List<ToolDefinition> tools;
    private boolean stream;
}
