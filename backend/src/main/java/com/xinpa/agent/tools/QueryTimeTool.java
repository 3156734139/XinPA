package com.xinpa.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryTimeTool implements AgentTool {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm");

    private final ObjectMapper objectMapper;

    public QueryTimeTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "query_current_time";
    }

    @Override
    public String getDescription() {
        return "获取当前日期和时间（北京时间），无需参数";
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
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("date", today.format(DATE_FMT));
            result.put("datetime", now.format(DATETIME_FMT));
            result.put("iso_date", today.toString());
            result.put("iso_datetime", now.toString());
            result.put("day_of_week", getChineseWeekday(today.getDayOfWeek().getValue()));
            result.put("timezone", "Asia/Shanghai (UTC+8)");

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getChineseWeekday(int dayOfWeek) {
        String[] names = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? names[dayOfWeek] : "";
    }
}
