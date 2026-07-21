package com.xinpa.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ToolCall {
    private String id;
    private String type = "function";
    @JsonProperty("function")
    private FunctionCall function;

    @Data
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
}
