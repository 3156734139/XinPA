package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.entity.OrderSource;
import com.xinpa.service.OrderSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单来源（用户端下拉选单用）
 */
@RestController
@RequestMapping("/order-sources")
@RequiredArgsConstructor
public class OrderSourceController {

    private final OrderSourceService orderSourceService;

    /**
     * 已启用的来源列表
     */
    @GetMapping("/list-enabled")
    public Result<List<OrderSource>> listEnabled() {
        return Result.ok(orderSourceService.listEnabled());
    }
}
