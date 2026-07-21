package com.xinpa.agent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.agent.dto.AgentResponse;
import com.xinpa.agent.dto.ChatMessage;
import com.xinpa.agent.dto.ChatResponse;
import com.xinpa.agent.dto.ToolCall;
import com.xinpa.agent.dto.ToolDefinition;
import com.xinpa.agent.tools.AgentTool;
import com.xinpa.agent.tools.CreateOrderTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private static final int MAX_ROUNDS = 10;
    private static final String SESSION_PREFIX = "agent:session:";
    private static final long SESSION_TTL_HOURS = 1;

    private final LlmService llmService;
    private final List<AgentTool> tools;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public AgentResponse chat(Long userId, String userMessage) {
        String key = SESSION_PREFIX + userId;
        List<ChatMessage> messages = getSession(key);

        messages.add(ChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());

        List<ToolDefinition> toolDefs = getToolDefinitions();

        for (int round = 0; round < MAX_ROUNDS; round++) {
            ChatResponse response = llmService.chat(messages, toolDefs);
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                saveSession(key, messages);
                return AgentResponse.chat("抱歉，AI 服务暂未响应，请检查 LLM 配置。");
            }

            ChatMessage responseMsg = response.getChoices().get(0).getMessage();

            if (responseMsg.getToolCalls() != null && !responseMsg.getToolCalls().isEmpty()) {
                messages.add(responseMsg);

                for (ToolCall tc : responseMsg.getToolCalls()) {
                    if (tc.getFunction() == null) continue;
                    log.info("Tool call: {} with args: {}", tc.getFunction().getName(), tc.getFunction().getArguments());

                    String result = executeTool(tc.getFunction().getName(),
                            tc.getFunction().getArguments(), userId);

                    // 检测工具是否返回了特殊类型（如 order_preview）
                    AgentResponse special = checkSpecialResponse(result);
                    if (special != null) {
                        saveSession(key, messages);
                        return special;
                    }

                    messages.add(ChatMessage.builder()
                            .role("tool")
                            .toolCallId(tc.getId())
                            .content(result)
                            .build());
                }
            } else {
                // 最终文本回复
                messages.add(responseMsg);
                saveSession(key, messages);
                return AgentResponse.chat(responseMsg.getContent());
            }
        }

        saveSession(key, messages);
        return AgentResponse.chat("抱歉，处理超时，请简化您的问题后重试。");
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> getSession(String key) {
        List<ChatMessage> messages = (List<ChatMessage>) redisTemplate.opsForValue().get(key);
        if (messages == null) {
            messages = createSession();
        }
        return messages;
    }

    private void saveSession(String key, List<ChatMessage> messages) {
        redisTemplate.opsForValue().set(key, messages, Duration.ofHours(SESSION_TTL_HOURS));
    }

    public Map<String, Object> confirmOrder(String token, Map<String, Object> edits, Long userId) {
        CreateOrderTool tool = (CreateOrderTool) tools.stream()
                .filter(t -> t instanceof CreateOrderTool)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("CreateOrderTool not found"));
        return tool.confirmOrder(token, edits, userId);
    }

    public void clearHistory(Long userId) {
        redisTemplate.delete(SESSION_PREFIX + userId);
    }

    @SuppressWarnings("unchecked")
    public List<ChatMessage> getHistory(Long userId) {
        List<ChatMessage> history = (List<ChatMessage>) redisTemplate.opsForValue().get(SESSION_PREFIX + userId);
        if (history == null) return List.of();
        return history.stream()
                .filter(m -> !"system".equals(m.getRole()))
                .collect(Collectors.toList());
    }

    private List<ChatMessage> createSession() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(ChatMessage.builder()
                .role("system")
                .content(buildSystemPrompt())
                .build());
        return list;
    }

    private String executeTool(String name, String argsJson, Long userId) {
        for (AgentTool tool : tools) {
            if (tool.getName().equals(name)) {
                return tool.execute(userId, argsJson);
            }
        }
        return "{\"error\": \"未知工具: " + name + "\"}";
    }

    /**
     * 检查工具返回结果中是否包含特殊类型标记（如 order_preview）
     */
    private AgentResponse checkSpecialResponse(String result) {
        if (result == null || !result.contains("\"_type\"")) return null;
        try {
            Map<String, Object> map = objectMapper.readValue(result,
                    new TypeReference<Map<String, Object>>() {});
            String type = (String) map.get("_type");
            if ("order_preview".equals(type)) {
                return AgentResponse.orderPreview(map);
            }
        } catch (Exception e) {
            log.warn("Failed to parse special response", e);
        }
        return null;
    }

    private List<ToolDefinition> getToolDefinitions() {
        return tools.stream().map(t -> ToolDefinition.builder()
                .function(ToolDefinition.FunctionDefinition.builder()
                        .name(t.getName())
                        .description(t.getDescription())
                        .parameters(t.getParameters())
                        .build())
                .build()
        ).collect(Collectors.toList());
    }

    private String buildSystemPrompt() {
        return """
                你是一个陪玩星助手AI助理，帮助陪玩从业者管理业务数据。
                你可以查询和创建客户信息、订单、财务记录、预约日程等。

                请用中文回答，语言简洁友好。当用户的问题涉及数据查询时，请使用提供的工具获取实时数据。
                如果用户的问题无法通过现有工具解决，请如实告知能力范围。
                创建订单时，请先通过其他工具收集完整信息（客户、时间、价格），再调用 create_order。

                工具清单：
                - query_customer: 根据昵称或联系方式关键字查询客户
                - create_customer: 添加新客户（需昵称和联系方式）
                - update_customer: 更新客户信息（需提供客户ID）
                - query_orders: 查询订单列表（支持按日期、金额筛选）
                - create_order: 创建订单（两阶段：先预览确认，后真实创建）
                - update_order: 更新订单信息（时间、单价、支付方式等）
                - query_finance: 查询财务统计（收入/支出汇总及趋势）
                - create_finance_record: 记账（记录一笔收入或支出）
                - query_finance_setting: 查询财务设置（月目标、预算等）
                - update_finance_setting: 更新月收入目标或月支出预算
                - query_stats: 业务概览数据（客户数、本月收入等）
                - query_vip_config: 查询VIP等级配置
                - query_packages: 查询价目套餐列表
                - query_todos: 查询待办事项列表（可按状态筛选）
                - create_todo: 创建新的待办事项
                - query_current_time: 获取当前准确日期和时间（北京时间），当用户询问"今天""现在"等时间相关问题时必须调用此工具获取实时时间，不要依赖自身知识中的默认时间

                注意事项：
                1. 使用工具后，根据返回的数据组织回答，不要编造数据。
                2. 如果工具返回空数据，请如实告知用户。
                3. 创建待办/客户/记账后需要告知用户创建成功及内容。
                4. 创建订单时，调用 create_order 后系统会返回预确认信息，请勿重复创建。
                5. 涉及日期时间的问题，必须先调用 query_current_time 获取实时时间。
                """;
    }
}
