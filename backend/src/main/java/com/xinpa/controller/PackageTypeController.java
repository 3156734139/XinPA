package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.entity.PackageType;
import com.xinpa.service.PackageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐类型（用户端下拉选单用）
 */
@RestController
@RequestMapping("/package-types")
@RequiredArgsConstructor
public class PackageTypeController {

    private final PackageTypeService packageTypeService;

    /**
     * 已启用的套餐类型列表
     */
    @GetMapping("/list-enabled")
    public Result<List<PackageType>> listEnabled() {
        return Result.ok(packageTypeService.listEnabled());
    }
}
