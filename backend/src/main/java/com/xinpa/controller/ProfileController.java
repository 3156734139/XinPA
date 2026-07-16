package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.PricePackage;
import com.xinpa.entity.UserGameConfig;
import com.xinpa.entity.UserProfile;
import com.xinpa.service.MaterialService;
import com.xinpa.service.OrderService;
import com.xinpa.service.PricePackageService;
import com.xinpa.service.UserGameConfigService;
import com.xinpa.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人设主页管理接口
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;
    private final PricePackageService pricePackageService;
    private final MaterialService materialService;
    private final UserGameConfigService userGameConfigService;
    private final OrderService orderService;

    // ==================== 人设主页 ====================

    /**
     * 获取主页信息
     */
    @GetMapping
    public Result<UserProfile> getProfile() {
        return Result.ok(userProfileService.getByUserId(UserContext.getUserId()));
    }

    /**
     * 更新主页信息
     */
    @PutMapping
    public Result<Void> updateProfile(@RequestBody UserProfile profile) {
        profile.setUserId(UserContext.getUserId());
        userProfileService.update(profile);
        return Result.ok();
    }

    /**
     * 切换接单状态
     */
    @PutMapping("/order-status")
    public Result<Void> switchOrderStatus(@RequestParam Integer status) {
        userProfileService.updateOrderStatus(UserContext.getUserId(), status);
        return Result.ok();
    }

    // ==================== 价目套餐 ====================

    /**
     * 获取套餐列表
     */
    @GetMapping("/packages")
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
    @PostMapping("/packages")
    public Result<Void> addPackage(@RequestBody PricePackage pkg) {
        pkg.setUserId(UserContext.getUserId());
        pricePackageService.add(pkg);
        return Result.ok();
    }

    /**
     * 更新套餐
     */
    @PutMapping("/packages")
    public Result<Void> updatePackage(@RequestBody PricePackage pkg) {
        pricePackageService.update(pkg);
        return Result.ok();
    }

    /**
     * 删除套餐
     */
    @DeleteMapping("/packages/{id}")
    public Result<Void> deletePackage(@PathVariable Long id) {
        pricePackageService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    // ==================== 素材管理 ====================

    /**
     * 获取素材列表
     */
    @GetMapping("/materials")
    public Result<?> listMaterials(@RequestParam(required = false) Integer type) {
        return Result.ok(materialService.listByUserId(UserContext.getUserId(), type));
    }

    /**
     * 上传素材
     */
    @PostMapping("/materials/upload")
    public Result<?> uploadMaterial(
            @RequestParam String name,
            @RequestParam Integer type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0") Integer watermark) {
        return Result.ok(materialService.upload(UserContext.getUserId(), name, type, file, watermark));
    }

    /**
     * 删除素材
     */
    @DeleteMapping("/materials/{id}")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        materialService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    // ==================== 游戏配置 ====================

    /**
     * 获取所有游戏配置列表
     */
    @GetMapping("/game-configs")
    public Result<?> listGameConfigs() {
        return Result.ok(userGameConfigService.listByUserId(UserContext.getUserId()));
    }

    /**
     * 添加游戏配置
     */
    @PostMapping("/game-configs")
    public Result<Void> addGameConfig(@RequestBody UserGameConfig config) {
        config.setUserId(UserContext.getUserId());
        userGameConfigService.add(config);
        return Result.ok();
    }

    /**
     * 更新游戏配置
     */
    @PutMapping("/game-configs")
    public Result<Void> updateGameConfig(@RequestBody UserGameConfig config) {
        config.setUserId(UserContext.getUserId());
        userGameConfigService.update(config);
        return Result.ok();
    }

    /**
     * 删除游戏配置
     */
    @DeleteMapping("/game-configs/{id}")
    public Result<Void> deleteGameConfig(@PathVariable Long id) {
        userGameConfigService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
