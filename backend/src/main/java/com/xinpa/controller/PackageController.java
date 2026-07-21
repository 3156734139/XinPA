package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.PricePackage;
import com.xinpa.service.OrderService;
import com.xinpa.service.PricePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 价目套餐接口
 */
@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PricePackageService pricePackageService;
    private final OrderService orderService;

    /**
     * 获取套餐列表（含统计）
     */
    @GetMapping
    public Result<?> listPackages() {
        Long userId = UserContext.getUserId();
        List<PricePackage> packages = pricePackageService.listByUserId(userId);
        List<Map<String, Object>> stats = orderService.getPackageStats(userId);

        Map<Long, Map<String, Object>> statsMap = stats.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("package_id")).longValue(),
                        m -> m
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (PricePackage pkg : packages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", pkg.getId());
            item.put("name", pkg.getName());
            item.put("packageType", pkg.getPackageType());
            item.put("price", pkg.getPrice());
            item.put("unit", pkg.getUnit());
            item.put("description", pkg.getDescription());
            item.put("discountRules", pkg.getDiscountRules());
            item.put("sortOrder", pkg.getSortOrder());
            item.put("status", pkg.getStatus());
            item.put("createdAt", pkg.getCreatedAt());

            Map<String, Object> stat = statsMap.get(pkg.getId());
            if (stat != null) {
                item.put("orderCount", ((Number) stat.get("order_count")).longValue());
                item.put("totalRevenue", stat.get("total_revenue"));
            } else {
                item.put("orderCount", 0);
                item.put("totalRevenue", BigDecimal.ZERO);
            }

            result.add(item);
        }

        return Result.ok(result);
    }

    /**
     * 添加套餐
     */
    @PostMapping
    public Result<Void> addPackage(@RequestBody PricePackage pkg) {
        pkg.setUserId(UserContext.getUserId());
        pricePackageService.add(pkg);
        return Result.ok();
    }

    /**
     * 更新套餐
     */
    @PutMapping
    public Result<Void> updatePackage(@RequestBody PricePackage pkg) {
        pkg.setUserId(UserContext.getUserId());
        pricePackageService.update(pkg);
        return Result.ok();
    }

    /**
     * 删除套餐
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePackage(@PathVariable Long id) {
        pricePackageService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
