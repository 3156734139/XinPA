package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.service.FinanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 财务管理接口
 */
@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceRecordService financeRecordService;

    /**
     * 分页查询流水
     */
    @GetMapping("/records")
    public Result<PageResult<FinanceRecord>> records(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(PageResult.of(
                financeRecordService.page(UserContext.getUserId(), type, start, end, current, size)));
    }

    /**
     * 新增流水
     */
    @PostMapping("/records")
    public Result<Void> create(@Valid @RequestBody FinanceRecord record) {
        record.setUserId(UserContext.getUserId());
        financeRecordService.create(record);
        return Result.ok();
    }

    /**
     * 删除流水
     */
    @DeleteMapping("/records/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        financeRecordService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 收益统计（日/周/月）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();

        BigDecimal todayIncome = financeRecordService.sumIncome(userId, today, today);
        BigDecimal todayExpense = financeRecordService.sumExpense(userId, today, today);
        BigDecimal weekIncome = financeRecordService.sumIncome(userId, today.with(java.time.DayOfWeek.MONDAY), today);
        BigDecimal monthIncome = financeRecordService.sumIncome(userId, today.withDayOfMonth(1), today);
        BigDecimal monthExpense = financeRecordService.sumExpense(userId, today.withDayOfMonth(1), today);

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayIncome", todayIncome);
        stats.put("todayExpense", todayExpense);
        stats.put("weekIncome", weekIncome);
        stats.put("monthIncome", monthIncome);
        stats.put("monthExpense", monthExpense);
        stats.put("monthProfit", monthIncome.subtract(monthExpense));
        return Result.ok(stats);
    }

    /**
     * 收支趋势图（支持日/周/月聚合）
     */
    @GetMapping("/trend")
    public Result<?> trend(@RequestParam(defaultValue = "day") String mode,
                           @RequestParam(required = false) LocalDate start,
                           @RequestParam(required = false) LocalDate end) {
        return Result.ok(financeRecordService.trend(UserContext.getUserId(), mode, start, end));
    }

    /**
     * 获取财务设置
     */
    @GetMapping("/setting")
    public Result<UserFinanceSetting> getSetting() {
        return Result.ok(financeRecordService.getSetting(UserContext.getUserId()));
    }

    /**
     * 更新财务设置
     */
    @PutMapping("/setting")
    public Result<Void> updateSetting(@RequestBody UserFinanceSetting setting) {
        setting.setUserId(UserContext.getUserId());
        financeRecordService.updateSetting(setting);
        return Result.ok();
    }

    /**
     * 提现手续费计算器
     */
    @GetMapping("/withdraw-calc")
    public Result<Map<String, Object>> withdrawCalc(@RequestParam BigDecimal amount) {
        UserFinanceSetting setting = financeRecordService.getSetting(UserContext.getUserId());
        BigDecimal fee = amount.multiply(setting.getWithdrawFeeRate());
        BigDecimal actual = amount.subtract(fee);

        Map<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("feeRate", setting.getWithdrawFeeRate());
        result.put("fee", fee);
        result.put("actualAmount", actual);
        return Result.ok(result);
    }
}
