package com.xinpa.agent.tools;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    Map<String, Object> getParameters();
    String execute(Long userId, String argsJson);
}
