package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.PaymentMethod;
import com.xinpa.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付方式（用户端）
 */
@RestController
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    /**
     * 已启用的支付方式列表（下拉选单用）
     */
    @GetMapping("/list-enabled")
    public Result<List<PaymentMethod>> listEnabled() {
        return Result.ok(paymentMethodService.listEnabled(UserContext.getUserId()));
    }

    /**
     * 用户的支付方式列表（管理用，含禁用）
     */
    @GetMapping
    public Result<List<PaymentMethod>> list() {
        return Result.ok(paymentMethodService.listByUser(UserContext.getUserId()));
    }

    /**
     * 新增支付方式
     */
    @PostMapping
    public Result<Void> create(@RequestBody PaymentMethod paymentMethod) {
        paymentMethodService.create(paymentMethod, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 更新支付方式
     */
    @PutMapping
    public Result<Void> update(@RequestBody PaymentMethod paymentMethod) {
        paymentMethodService.update(paymentMethod, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 删除支付方式
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
