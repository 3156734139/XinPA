package com.xinpa.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.service.CustomerService;
import com.xinpa.service.FinanceRecordService;
import com.xinpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryStatsTool implements AgentTool {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final FinanceRecordService financeRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_stats";
    }

    @Override
    public String getDescription() {
        return "获取业务统计概览：客户总数、本月收入、订单总量等";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(),
                "required", List.of()
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            long customerCount = customerService.countByUserId(userId);
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate today = LocalDate.now();
            BigDecimal monthIncome = financeRecordService.sumIncome(userId, monthStart, today);
            BigDecimal monthExpense = financeRecordService.sumExpense(userId, monthStart, today);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("customerCount", customerCount);
            data.put("monthIncome", monthIncome);
            data.put("monthExpense", monthExpense);
            data.put("monthNetIncome", monthIncome.subtract(monthExpense));

            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
