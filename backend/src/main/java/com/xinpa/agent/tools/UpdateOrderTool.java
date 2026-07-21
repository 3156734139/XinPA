package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.Order;
import com.xinpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateOrderTool implements AgentTool {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "update_order";
    }

    @Override
    public String getDescription() {
        return "更新订单信息。需要提供订单ID，其他字段按需传入要修改的值。可修改时间、单价、支付方式、到手比例、备注等。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "number", "description", "订单ID（必填）"));
        properties.put("startTime", Map.of("type", "string", "description", "开始时间，格式 yyyy-MM-dd HH:mm（可选）"));
        properties.put("endTime", Map.of("type", "string", "description", "结束时间，格式 yyyy-MM-dd HH:mm（可选）"));
        properties.put("unitPrice", Map.of("type", "number", "description", "单价（可选）"));
        properties.put("paymentMethod", Map.of("type", "string", "description", "支付方式（可选）"));
        properties.put("settleRatio", Map.of("type", "number", "description", "到手比例，1-100（可选）"));
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
                return "{\"error\": \"缺少订单ID\"}";
            }
            Long id = ((Number) args.get("id")).longValue();

            Order order = orderService.getById(id);
            if (order == null) {
                return "{\"error\": \"订单不存在\"}";
            }

            boolean changed = false;
            if (args.containsKey("startTime")) {
                order.setStartTime(LocalDateTime.parse((String) args.get("startTime"), DTF));
                changed = true;
            }
            if (args.containsKey("endTime")) {
                order.setEndTime(LocalDateTime.parse((String) args.get("endTime"), DTF));
                changed = true;
            }
            if (args.containsKey("unitPrice")) {
                order.setUnitPrice(new BigDecimal(args.get("unitPrice").toString()));
                changed = true;
            }
            if (args.containsKey("paymentMethod")) {
                order.setPaymentMethod((String) args.get("paymentMethod"));
                changed = true;
            }
            if (args.containsKey("settleRatio")) {
                order.setSettleRatio(new BigDecimal(args.get("settleRatio").toString()));
                changed = true;
            }
            if (args.containsKey("remark")) {
                order.setRemark((String) args.get("remark"));
                changed = true;
            }

            if (!changed) {
                return "{\"error\": \"没有需要修改的字段\"}";
            }

            orderService.update(order);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "id", order.getId(),
                    "orderNo", order.getOrderNo()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
