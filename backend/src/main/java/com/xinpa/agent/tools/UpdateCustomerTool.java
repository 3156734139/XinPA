package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.Customer;
import com.xinpa.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateCustomerTool implements AgentTool {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "update_customer";
    }

    @Override
    public String getDescription() {
        return "更新客户信息。需要提供客户ID，其他字段按需传入要修改的值。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "number", "description", "客户ID（必填）"));
        properties.put("nickname", Map.of("type", "string", "description", "客户昵称（可选）"));
        properties.put("contact", Map.of("type", "string", "description", "联系方式（可选）"));
        properties.put("sourceId", Map.of("type", "number", "description", "来源ID（可选）"));
        properties.put("personality", Map.of("type", "string", "description", "性格/爱好/雷点（可选）"));
        properties.put("remark", Map.of("type", "string", "description", "备注（可选）"));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("id")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            if (!args.containsKey("id")) {
                return "{\"error\": \"缺少客户ID\"}";
            }
            Long id = ((Number) args.get("id")).longValue();

            Customer existing = customerService.getById(id);
            if (existing == null) {
                return "{\"error\": \"客户不存在\"}";
            }

            if (args.containsKey("nickname")) {
                existing.setNickname((String) args.get("nickname"));
            }
            if (args.containsKey("contact")) {
                existing.setContact((String) args.get("contact"));
            }
            if (args.containsKey("sourceId")) {
                existing.setSourceId(((Number) args.get("sourceId")).longValue());
            }
            if (args.containsKey("personality")) {
                existing.setPersonality((String) args.get("personality"));
            }
            if (args.containsKey("remark")) {
                existing.setRemark((String) args.get("remark"));
            }

            customerService.update(existing);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "id", existing.getId(),
                    "nickname", existing.getNickname()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
