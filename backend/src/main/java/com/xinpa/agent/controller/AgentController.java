package com.xinpa.agent.controller;

import com.xinpa.agent.dto.AgentResponse;
import com.xinpa.agent.dto.ChatMessage;
import com.xinpa.agent.service.AgentOrchestrator;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;

    /**
     * 发送聊天消息
     * POST /api/agent/chat
     * Body: {"message": "查询我的客户信息"}
     */
    @PostMapping("/chat")
    public Result<AgentResponse> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return Result.fail("消息不能为空");
        }
        AgentResponse reply = agentOrchestrator.chat(UserContext.getUserId(), message.trim());
        return Result.ok(reply);
    }

    /**
     * 确认创建订单
     * POST /api/agent/order/confirm
     * Body: {"confirmToken": "xxx", "edits": {...}}
     */
    @PostMapping("/order/confirm")
    public Result<Map<String, Object>> confirmOrder(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("confirmToken");
        if (token == null || token.isBlank()) {
            return Result.fail("缺少确认凭证");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> edits = body.get("edits") instanceof Map
                ? (Map<String, Object>) body.get("edits") : null;
        Map<String, Object> result = agentOrchestrator.confirmOrder(token, edits, UserContext.getUserId());
        boolean success = Boolean.TRUE.equals(result.get("success"));
        if (!success) {
            String error = (String) result.get("error");
            return Result.fail(error != null ? error : "创建失败");
        }
        return Result.ok(result);
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> history() {
        return Result.ok(agentOrchestrator.getHistory(UserContext.getUserId()));
    }

    /**
     * 清空对话历史
     */
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        agentOrchestrator.clearHistory(UserContext.getUserId());
        return Result.ok();
    }
}
