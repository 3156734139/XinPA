package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.OrderSource;
import com.xinpa.service.OrderSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单来源（用户端）
 */
@RestController
@RequestMapping("/order-sources")
@RequiredArgsConstructor
public class OrderSourceController {

    private final OrderSourceService orderSourceService;

    /**
     * 已启用的来源列表（订单下拉选单用）
     */
    @GetMapping("/list-enabled")
    public Result<List<OrderSource>> listEnabled() {
        return Result.ok(orderSourceService.listEnabled(UserContext.getUserId()));
    }

    /**
     * 来源列表（管理用，含禁用）
     */
    @GetMapping
    public Result<List<OrderSource>> list() {
        return Result.ok(orderSourceService.listByUser(UserContext.getUserId()));
    }

    /**
     * 新增来源
     */
    @PostMapping
    public Result<Void> create(@RequestBody OrderSource orderSource) {
        orderSourceService.create(orderSource, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 更新来源
     */
    @PutMapping
    public Result<Void> update(@RequestBody OrderSource orderSource) {
        orderSourceService.update(orderSource, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 删除来源
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderSourceService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
