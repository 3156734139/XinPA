package com.xinpa.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.VipLevel;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryVipConfigTool implements AgentTool {

    private final VipLevelService vipLevelService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_vip_config";
    }

    @Override
    public String getDescription() {
        return "查询VIP等级配置，包括各等级的门槛金额和折扣率";
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
            List<VipLevel> levels = vipLevelService.listEnabled(userId);
            List<Map<String, Object>> data = levels.stream().map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("level", v.getLevel());
                m.put("name", v.getName());
                m.put("threshold", v.getThreshold());
                m.put("discount", v.getDiscount());
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(Map.of("vipLevels", data));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
