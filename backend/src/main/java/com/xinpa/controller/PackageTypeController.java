package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.PackageType;
import com.xinpa.service.PackageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐类型（用户端）
 */
@RestController
@RequestMapping("/package-types")
@RequiredArgsConstructor
public class PackageTypeController {

    private final PackageTypeService packageTypeService;

    /**
     * 已启用的套餐类型列表（下拉选单用）
     */
    @GetMapping("/list-enabled")
    public Result<List<PackageType>> listEnabled() {
        return Result.ok(packageTypeService.listEnabled(UserContext.getUserId()));
    }

    /**
     * 用户的套餐类型列表（管理用，含禁用）
     */
    @GetMapping
    public Result<List<PackageType>> list() {
        return Result.ok(packageTypeService.listByUser(UserContext.getUserId()));
    }

    /**
     * 新增套餐类型
     */
    @PostMapping
    public Result<Void> create(@RequestBody PackageType packageType) {
        packageTypeService.create(packageType, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 更新套餐类型
     */
    @PutMapping
    public Result<Void> update(@RequestBody PackageType packageType) {
        packageTypeService.update(packageType, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 删除套餐类型
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        packageTypeService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
