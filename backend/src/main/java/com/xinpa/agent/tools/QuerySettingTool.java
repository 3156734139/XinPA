package com.xinpa.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.service.FinanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuerySettingTool implements AgentTool {

    private final FinanceRecordService financeRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_finance_setting";
    }

    @Override
    public String getDescription() {
        return "查询财务设置：月收入目标、月支出预算、提现手续费率。";
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
            UserFinanceSetting setting = financeRecordService.getSetting(userId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("monthlyTarget", setting.getMonthlyTarget());
            result.put("monthlyExpenseTarget", setting.getMonthlyExpenseTarget());
            result.put("withdrawFeeRate", setting.getWithdrawFeeRate());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
