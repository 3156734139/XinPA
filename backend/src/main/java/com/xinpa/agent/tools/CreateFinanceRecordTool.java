package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.FinanceRecord;
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
public class CreateFinanceRecordTool implements AgentTool {

    private final FinanceRecordService financeRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "create_finance_record";
    }

    @Override
    public String getDescription() {
        return "记账：记录一笔收入或支出。需要类型、金额、分类。可选日期、支付方式、备注。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("recordType", Map.of("type", "integer", "description", "类型：1收入 2支出（必填）"));
        properties.put("amount", Map.of("type", "number", "description", "金额（必填）"));
        properties.put("category", Map.of("type", "string", "description", "分类名称，如陪玩收入、平台抽成等（必填）"));
        properties.put("paymentMethod", Map.of("type", "string", "description", "支付方式：平台/微信/支付宝/现金（可选）"));
        properties.put("recordDate", Map.of("type", "string", "description", "日期，格式 yyyy-MM-dd，默认今天（可选）"));
        properties.put("remark", Map.of("type", "string", "description", "备注（可选）"));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("recordType", "amount", "category")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            if (!args.containsKey("recordType")) {
                return "{\"error\": \"缺少类型：recordType\"}";
            }
            if (!args.containsKey("amount")) {
                return "{\"error\": \"缺少金额：amount\"}";
            }
            if (!args.containsKey("category")) {
                return "{\"error\": \"缺少分类：category\"}";
            }

            FinanceRecord record = new FinanceRecord();
            record.setUserId(userId);
            record.setRecordType(((Number) args.get("recordType")).intValue());
            record.setAmount(new BigDecimal(args.get("amount").toString()));
            record.setCategory((String) args.get("category"));

            if (args.containsKey("paymentMethod")) {
                record.setPaymentMethod((String) args.get("paymentMethod"));
            }
            if (args.containsKey("recordDate")) {
                record.setRecordDate(LocalDate.parse((String) args.get("recordDate")));
            } else {
                record.setRecordDate(LocalDate.now());
            }
            if (args.containsKey("remark")) {
                record.setRemark((String) args.get("remark"));
            }

            financeRecordService.create(record);

            return objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "id", record.getId(),
                    "recordType", record.getRecordType() == 1 ? "收入" : "支出",
                    "amount", record.getAmount(),
                    "category", record.getCategory()
            ));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
