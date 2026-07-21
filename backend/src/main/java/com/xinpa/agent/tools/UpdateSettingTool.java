package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.service.FinanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateSettingTool implements AgentTool {

    private final FinanceRecordService financeRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "update_finance_setting";
    }

    @Override
    public String getDescription() {
        return "更新财务设置：月收入目标、月支出预算。需要提供用户ID，其他字段按需传入。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("monthlyTarget", Map.of("type", "number", "description", "月收入目标金额（可选）"));
        properties.put("monthlyExpenseTarget", Map.of("type", "number", "description", "月支出预算金额（可选）"));
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

            UserFinanceSetting setting = financeRecordService.getSetting(userId);
            if (setting == null) {
                throw new BusinessException("财务设置不存在");
            }

            boolean changed = false;
            if (args.containsKey("monthlyTarget")) {
                setting.setMonthlyTarget(new BigDecimal(args.get("monthlyTarget").toString()));
                changed = true;
            }
            if (args.containsKey("monthlyExpenseTarget")) {
                setting.setMonthlyExpenseTarget(new BigDecimal(args.get("monthlyExpenseTarget").toString()));
                changed = true;
            }

            if (!changed) {
                return "{\"error\": \"没有需要修改的字段\"}";
            }

            financeRecordService.updateSetting(setting);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "monthlyTarget", setting.getMonthlyTarget(),
                    "monthlyExpenseTarget", setting.getMonthlyExpenseTarget()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
