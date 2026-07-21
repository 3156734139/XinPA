package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.TodoItem;
import com.xinpa.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryTodosTool implements AgentTool {

    private final TodoItemService todoItemService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_todos";
    }

    @Override
    public String getDescription() {
        return "查询待办事项列表，可按状态筛选（0=待办，1=已完成）";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "status", Map.of(
                                "type", "integer",
                                "description", "状态筛选：0待办 1已完成，不传则查全部"
                        )
                ),
                "required", List.of()
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});
            Integer status = args.containsKey("status") ? ((Number) args.get("status")).intValue() : null;

            List<TodoItem> items = todoItemService.listByUserId(userId, status);

            List<Map<String, Object>> data = items.stream().map(t -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", t.getId());
                m.put("title", t.getTitle());
                m.put("todoType", t.getTodoType());
                m.put("status", t.getStatus());
                m.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : null);
                m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(Map.of("todos", data));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
