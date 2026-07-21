package com.xinpa.controller.admin;

import com.xinpa.common.Result;
import com.xinpa.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员 - 平台统计接口（只读聚合数据）
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    /**
     * 平台总览大屏
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(statsService.getOverview());
    }

    /**
     * 用户增长曲线
     */
    @GetMapping("/user-growth")
    public Result<?> userGrowth() {
        return Result.ok(statsService.getUserGrowth());
    }

    /**
     * 套餐类型占比
     */
    @GetMapping("/package-ratio")
    public Result<?> packageRatio() {
        return Result.ok(statsService.getPackageTypeRatio());
    }
}
