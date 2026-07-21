package com.xinpa.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;
    @JsonProperty("tool_call_id")
    private String toolCallId;
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
}
