package com.xinpa.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ToolDefinition {
    @Builder.Default
    private String type = "function";
    private FunctionDefinition function;

    @Data
    @Builder
    public static class FunctionDefinition {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }
}
