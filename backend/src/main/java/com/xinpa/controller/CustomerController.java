package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.Customer;
import com.xinpa.entity.FollowUpReminder;
import com.xinpa.entity.VipLevel;
import com.xinpa.service.CustomerService;
import com.xinpa.service.FollowUpReminderService;
import com.xinpa.service.VipLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 客户管理接口
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final FollowUpReminderService followUpReminderService;
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
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(PageResult.of(
                customerService.page(UserContext.getUserId(), keyword, sourceId,
                        spendLevel, minSpend, maxSpend, minOrders, maxOrders, blacklist, current, size)));
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
        List<VipLevel> vipLevels = vipLevelService.listEnabled();
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

    // ==================== VIP 配置 ====================

    /**
     * 获取 VIP 等级配置列表（门槛和折扣）
     */
    @GetMapping("/vip-configs")
    public Result<List<VipLevel>> vipConfigs() {
        return Result.ok(vipLevelService.listEnabled());
    }

    // ==================== 回访提醒 ====================

    /**
     * 回访提醒列表
     */
    @GetMapping("/reminders")
    public Result<?> reminders(@RequestParam(required = false) Integer status) {
        return Result.ok(followUpReminderService.listByUserId(UserContext.getUserId(), status));
    }

    /**
     * 处理回访
     */
    @PostMapping("/reminders/{id}/handle")
    public Result<Void> handleReminder(@PathVariable Long id) {
        followUpReminderService.handle(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 忽略回访
     */
    @PostMapping("/reminders/{id}/ignore")
    public Result<Void> ignoreReminder(@PathVariable Long id) {
        followUpReminderService.ignore(id, UserContext.getUserId());
        return Result.ok();
    }
}
