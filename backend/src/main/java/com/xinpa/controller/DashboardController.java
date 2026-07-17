package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.service.CustomerService;
import com.xinpa.service.FinanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作台数据接口
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final FinanceRecordService financeRecordService;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;

    /**
     * 工作台统计（今日收入、今日订单、客户总数）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();

        BigDecimal todayIncome = financeRecordService.sumIncome(userId, today, today);
        long todayOrders = orderMapper.countTodayByUserId(userId, today);
        long totalCustomers = customerService.countByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayIncome", todayIncome);
        stats.put("todayOrders", todayOrders);
        stats.put("totalCustomers", totalCustomers);
        return Result.ok(stats);
    }
}
