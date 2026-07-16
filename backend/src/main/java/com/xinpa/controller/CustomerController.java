package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.Coupon;
import com.xinpa.entity.Customer;
import com.xinpa.entity.FollowUpReminder;
import com.xinpa.service.CouponService;
import com.xinpa.service.CustomerService;
import com.xinpa.service.FollowUpReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 客户管理接口
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CouponService couponService;
    private final FollowUpReminderService followUpReminderService;

    // ==================== 客户档案 ====================

    /**
     * 分页查询客户（支持多维度筛选）
     */
    @GetMapping
    public Result<PageResult<Customer>> page(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String contact,
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
                customerService.page(UserContext.getUserId(), nickname, contact, sourceId,
                        spendLevel, minSpend, maxSpend, minOrders, maxOrders, blacklist, current, size)));
    }

    /**
     * 客户详情
     */
    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        return Result.ok(customerService.getById(id));
    }

    /**
     * 创建客户
     */
    @PostMapping
    public Result<Void> create(@RequestBody Customer customer) {
        customer.setUserId(UserContext.getUserId());
        customerService.create(customer);
        return Result.ok();
    }

    /**
     * 更新客户
     */
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
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

    // ==================== 优惠券 ====================

    /**
     * 优惠券列表
     */
    @GetMapping("/coupons")
    public Result<?> coupons(@RequestParam(required = false) Long customerId) {
        return Result.ok(couponService.listByUserId(UserContext.getUserId(), customerId));
    }

    /**
     * 发放优惠券
     */
    @PostMapping("/coupons")
    public Result<Void> createCoupon(@RequestBody Coupon coupon) {
        coupon.setUserId(UserContext.getUserId());
        couponService.create(coupon);
        return Result.ok();
    }

    /**
     * 作废优惠券
     */
    @DeleteMapping("/coupons/{id}")
    public Result<Void> cancelCoupon(@PathVariable Long id) {
        couponService.cancel(id, UserContext.getUserId());
        return Result.ok();
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
