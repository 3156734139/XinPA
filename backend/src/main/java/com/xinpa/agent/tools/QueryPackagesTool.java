package com.xinpa.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.entity.PricePackage;
import com.xinpa.service.PricePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryPackagesTool implements AgentTool {

    private final PricePackageService pricePackageService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "query_packages";
    }

    @Override
    public String getDescription() {
        return "查询价目套餐列表，包含名称、单价、时长等信息";
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
            List<PricePackage> packages = pricePackageService.listByUserId(userId);
            List<Map<String, Object>> data = packages.stream().map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("name", p.getName());
                m.put("price", p.getPrice());
                m.put("unit", p.getUnit());
                m.put("packageType", p.getPackageType());
                m.put("status", p.getStatus());
                return m;
            }).collect(Collectors.toList());

            return objectMapper.writeValueAsString(Map.of("packages", data));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
