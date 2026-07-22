package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.Customer;
import com.xinpa.entity.VipLevel;
import com.xinpa.service.CustomerService;
import com.xinpa.service.VipLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户管理接口
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final VipLevelService vipLevelService;

    // ==================== 客户档案 ====================

    /**
     * 分页查询客户（支持多维度筛选）
     */
    @GetMapping
    public Result<PageResult<Customer>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) Integer spendLevel,
            @RequestParam(required = false) BigDecimal minSpend,
            @RequestParam(required = false) BigDecimal maxSpend,
            @RequestParam(required = false) Integer minOrders,
            @RequestParam(required = false) Integer maxOrders,
            @RequestParam(required = false) Integer blacklist,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return Result.ok(PageResult.of(
                customerService.page(UserContext.getUserId(), keyword, sourceId,
                        spendLevel, minSpend, maxSpend, minOrders, maxOrders, blacklist, current, size,
                        sortField, sortOrder)));
    }

    /**
     * 客户详情
     */
    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null || !customer.getUserId().equals(UserContext.getUserId())) {
            return Result.fail("客户不存在");
        }
        // 根据累计消费动态计算优惠等级（不依赖可能过期的 spendLevel 字段）
        BigDecimal total = customer.getTotalSpend();
        int level = 0;
        String levelName = null;
        Integer levelDiscount = null;
        List<VipLevel> vipLevels = vipLevelService.listEnabled(UserContext.getUserId());
        for (VipLevel vl : vipLevels) {
            if (total != null && total.compareTo(vl.getThreshold()) >= 0 && vl.getLevel() > level) {
                level = vl.getLevel();
                levelName = vl.getName();
                levelDiscount = vl.getDiscount();
            }
        }
        if (!Objects.equals(customer.getSpendLevel(), level)) {
            customer.setSpendLevel(level);
            customerService.updateSpendLevel(id, level);
        }
        customer.setSpendLevel(level);
        customer.setSpendLevelName(levelName);
        customer.setSpendLevelDiscount(levelDiscount);
        return Result.ok(customer);
    }

    /**
     * 创建客户
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody Customer customer) {
        customer.setUserId(UserContext.getUserId());
        customerService.create(customer);
        return Result.ok();
    }

    /**
     * 更新客户
     */
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customer.setUserId(UserContext.getUserId());
        customerService.update(customer);
        return Result.ok();
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 加入黑名单
     */
    @PostMapping("/{id}/blacklist")
    public Result<Void> addBlacklist(@PathVariable Long id, @RequestParam String reason) {
        customerService.addBlacklist(id, UserContext.getUserId(), reason);
        return Result.ok();
    }

    /**
     * 移出黑名单
     */
    @DeleteMapping("/{id}/blacklist")
    public Result<Void> removeBlacklist(@PathVariable Long id) {
        customerService.removeBlacklist(id, UserContext.getUserId());
        return Result.ok();
    }

    // ==================== 客户下拉列表（用于创建订单时选择） ====================

    /**
     * 获取所有客户简要列表（下拉框用）
     */
    @GetMapping("/list-all")
    public Result<?> listAll() {
        return Result.ok(customerService.listByUserId(UserContext.getUserId()));
    }

    /**
     * 获取 VIP 等级配置列表
     */
    @GetMapping("/vip-configs")
    public Result<List<VipLevel>> vipConfigs() {
        return Result.ok(vipLevelService.listEnabled(UserContext.getUserId()));
    }

    /**
     * 客户消费排行榜（按累计消费降序，取前20）
     */
    @GetMapping("/spending-ranking")
    public Result<List<Map<String, Object>>> spendingRanking() {
        Long userId = UserContext.getUserId();
        List<Customer> customers = customerService.getSpendingRanking(userId, 10);
        BigDecimal grandTotal = customers.stream()
                .map(Customer::getTotalSpend)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", i + 1);
            item.put("customerId", c.getId());
            item.put("nickname", c.getNickname() != null ? c.getNickname() : ("客户#" + c.getId()));
            item.put("totalSpend", c.getTotalSpend());
            item.put("orderCount", c.getOrderCount());
            item.put("lastOrderTime", c.getLastOrderTime());
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0 && c.getTotalSpend() != null) {
                item.put("percentage", c.getTotalSpend()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 1, RoundingMode.HALF_UP));
            } else {
                item.put("percentage", BigDecimal.ZERO);
            }
            ranking.add(item);
        }
        return Result.ok(ranking);
    }

}
