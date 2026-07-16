package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.entity.PaymentMethod;
import com.xinpa.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付方式（用户端下拉选单用）
 */
@RestController
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    /**
     * 已启用的支付方式列表
     */
    @GetMapping("/list-enabled")
    public Result<List<PaymentMethod>> listEnabled() {
        return Result.ok(paymentMethodService.listEnabled());
    }
}
