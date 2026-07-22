package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.VipLevel;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VIP等级（用户端）
 */
@RestController
@RequestMapping("/vip-levels")
@RequiredArgsConstructor
public class VipLevelController {

    private final VipLevelService vipLevelService;

    /**
     * 已启用的VIP等级列表（订单/Customer选单用）
     */
    @GetMapping("/list-enabled")
    public Result<List<VipLevel>> listEnabled() {
        return Result.ok(vipLevelService.listEnabled(UserContext.getUserId()));
    }

    /**
     * 用户的VIP等级列表（管理用，含禁用）
     */
    @GetMapping
    public Result<List<VipLevel>> list() {
        return Result.ok(vipLevelService.listByUser(UserContext.getUserId()));
    }

    /**
     * 新增VIP等级
     */
    @PostMapping
    public Result<Void> create(@RequestBody VipLevel vipLevel) {
        vipLevelService.create(vipLevel, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 更新VIP等级
     */
    @PutMapping
    public Result<Void> update(@RequestBody VipLevel vipLevel) {
        vipLevelService.update(vipLevel, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 删除VIP等级
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vipLevelService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
