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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryCustomerTool implements AgentTool {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_customer";
    }

    @Override
    public String getDescription() {
        return "根据昵称或联系方式关键字查询客户信息，返回匹配的客户列表（含累计消费、下单次数、优惠等级）";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("keyword", Map.of(
                "type", "string",
                "description", "客户昵称或联系方式关键字"
        ));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("keyword")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, String> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, String>>() {});
            String keyword = args.getOrDefault("keyword", "");

            List<Customer> customers = customerService.listByUserId(userId);
            if (!keyword.isBlank()) {
                String kw = keyword.toLowerCase();
                customers = customers.stream()
                        .filter(c -> (c.getNickname() != null && c.getNickname().toLowerCase().contains(kw))
                                || (c.getContact() != null && c.getContact().toLowerCase().contains(kw)))
                        .collect(Collectors.toList());
            }

            // 取前10条
            customers = customers.stream().limit(10).collect(Collectors.toList());

            List<Map<String, Object>> result = customers.stream().map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("nickname", c.getNickname());
                m.put("contact", c.getContact());
                m.put("totalSpend", c.getTotalSpend());
                m.put("orderCount", c.getOrderCount());
                m.put("spendLevel", c.getSpendLevel());
                m.put("isBlacklist", c.getIsBlacklist());
                m.put("lastOrderTime", c.getLastOrderTime());
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(Map.of(
                    "total", customers.size(),
                    "customers", result
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
