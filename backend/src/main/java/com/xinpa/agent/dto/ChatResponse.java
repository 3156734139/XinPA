package com.xinpa.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private int index;
        private ChatMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }
}
