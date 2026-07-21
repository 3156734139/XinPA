package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.TodoItem;
import com.xinpa.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateTodoTool implements AgentTool {

    private final TodoItemService todoItemService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "create_todo";
    }

    @Override
    public String getDescription() {
        return "创建一条待办事项，需要提供标题。可选：类型(1素材更新 2平台签到 3自定义)、截止日期(yyyy-MM-dd)";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "title", Map.of("type", "string", "description", "待办标题"),
                        "todoType", Map.of("type", "integer", "description", "类型：1素材更新 2平台签到 3自定义，默认3"),
                        "dueDate", Map.of("type", "string", "description", "截止日期，格式 yyyy-MM-dd（可选）")
                ),
                "required", List.of("title")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            String title = (String) args.get("title");
            if (title == null || title.isBlank()) {
                return "{\"error\": \"待办标题不能为空\"}";
            }

            TodoItem todo = new TodoItem();
            todo.setUserId(userId);
            todo.setTitle(title.trim());
            todo.setTodoType(args.containsKey("todoType")
                    ? ((Number) args.get("todoType")).intValue() : 3);
            todo.setStatus(0);
            if (args.containsKey("dueDate")) {
                todo.setDueDate(java.time.LocalDate.parse((String) args.get("dueDate")));
            }

            todoItemService.create(todo);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "id", todo.getId(),
                    "title", todo.getTitle()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
