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
public class CreateCustomerTool implements AgentTool {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "create_customer";
    }

    @Override
    public String getDescription() {
        return "添加新客户，需要提供昵称和联系方式。可选来源ID、性格爱好备注等。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("nickname", Map.of("type", "string", "description", "客户昵称（必填）"));
        properties.put("contact", Map.of("type", "string", "description", "联系方式，如微信/QQ（必填）"));
        properties.put("sourceId", Map.of("type", "number", "description", "来源ID（可选）：1=pw店 2=抖音 3=小红书 4=其他"));
        properties.put("personality", Map.of("type", "string", "description", "性格/爱好/雷点（可选）"));
        properties.put("remark", Map.of("type", "string", "description", "备注（可选）"));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("nickname", "contact")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            String nickname = (String) args.get("nickname");
            String contact = (String) args.get("contact");
            if (nickname == null || nickname.isBlank()) {
                return "{\"error\": \"客户昵称不能为空\"}";
            }
            if (contact == null || contact.isBlank()) {
                return "{\"error\": \"联系方式不能为空\"}";
            }

            Customer customer = new Customer();
            customer.setUserId(userId);
            customer.setNickname(nickname.trim());
            customer.setContact(contact.trim());
            if (args.containsKey("sourceId")) {
                customer.setSourceId(((Number) args.get("sourceId")).longValue());
            }
            if (args.containsKey("personality")) {
                customer.setPersonality((String) args.get("personality"));
            }
            if (args.containsKey("remark")) {
                customer.setRemark((String) args.get("remark"));
            }

            customerService.create(customer);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "id", customer.getId(),
                    "nickname", customer.getNickname()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
