package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.entity.Order;
import com.xinpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryOrdersTool implements AgentTool {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_orders";
    }

    @Override
    public String getDescription() {
        return "查询订单列表，支持按日期范围、金额范围、时长范围筛选";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("startDate", Map.of("type", "string", "description", "开始日期，格式 yyyy-MM-dd（可选）"));
        properties.put("endDate", Map.of("type", "string", "description", "结束日期，格式 yyyy-MM-dd（可选）"));
        properties.put("keyword", Map.of("type", "string", "description", "订单号或套餐名称关键字（可选）"));
        properties.put("pageSize", Map.of("type", "integer", "description", "返回条数，默认10（可选）"));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of()
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            OrderQueryDTO query = new OrderQueryDTO();
            query.setUserId(userId);
            if (args.containsKey("startDate")) {
                query.setStartDate(LocalDate.parse((String) args.get("startDate")));
            }
            if (args.containsKey("endDate")) {
                query.setEndDate(LocalDate.parse((String) args.get("endDate")));
            }
            if (args.containsKey("keyword")) {
                query.setKeyword((String) args.get("keyword"));
            }
            int size = args.containsKey("pageSize") ? ((Number) args.get("pageSize")).intValue() : 10;
            query.setSize(Math.min(size, 50));
            query.setCurrent(1);

            List<Order> orders = orderService.page(query).getRecords();

            List<Map<String, Object>> result = orders.stream().map(o -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("orderNo", o.getOrderNo());
                m.put("title", o.getTitle());
                m.put("packageName", o.getPackageName());
                m.put("actualMinutes", o.getActualMinutes());
                m.put("totalAmount", o.getTotalAmount());
                m.put("discountAmount", o.getDiscountAmount());
                m.put("finalAmount", o.getFinalAmount());
                m.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
                m.put("customerId", o.getCustomerId());
                m.put("customerName", o.getCustomerName());
                m.put("orderSource", o.getOrderSource());
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(Map.of(
                    "total", orders.size(),
                    "orders", result
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
