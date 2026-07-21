package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.service.FinanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryFinanceTool implements AgentTool {

    private final FinanceRecordService financeRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_finance";
    }

    @Override
    public String getDescription() {
        return "查询财务统计数据，包括收入、支出汇总及每日趋势";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("startDate", Map.of("type", "string", "description", "开始日期，格式 yyyy-MM-dd（可选）"));
        properties.put("endDate", Map.of("type", "string", "description", "结束日期，格式 yyyy-MM-dd（可选）"));
        properties.put("mode", Map.of("type", "string", "description", "趋势聚合模式：day/week/month，不传则只返回汇总"));
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

            LocalDate start = args.containsKey("startDate")
                    ? LocalDate.parse((String) args.get("startDate"))
                    : LocalDate.now().minusDays(30);
            LocalDate end = args.containsKey("endDate")
                    ? LocalDate.parse((String) args.get("endDate"))
                    : LocalDate.now();

            BigDecimal income = financeRecordService.sumIncome(userId, start, end);
            BigDecimal expense = financeRecordService.sumExpense(userId, start, end);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("startDate", start.toString());
            result.put("endDate", end.toString());
            result.put("totalIncome", income);
            result.put("totalExpense", expense);
            result.put("netIncome", income.subtract(expense));

            if (args.containsKey("mode")) {
                String mode = (String) args.get("mode");
                result.put("trend", financeRecordService.trend(userId, mode, start, end));
            }

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
