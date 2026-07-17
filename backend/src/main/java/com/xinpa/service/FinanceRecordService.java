package com.xinpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.UserFinanceSetting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务服务接口
 */
public interface FinanceRecordService {

    /**
     * 分页查询流水
     */
    Page<FinanceRecord> page(Long userId, Integer recordType, LocalDate start, LocalDate end, long current, long size);

    /**
     * 创建流水
     */
    void create(FinanceRecord record);

    /**
     * 删除流水
     */
    void delete(Long id, Long userId);

    /**
     * 统计收入
     */
    BigDecimal sumIncome(Long userId, LocalDate start, LocalDate end);

    /**
     * 统计支出
     */
    BigDecimal sumExpense(Long userId, LocalDate start, LocalDate end);

    /**
     * 按日统计收入趋势
     */
    List<Map<String, Object>> dailyIncome(Long userId, LocalDate start, LocalDate end);

    /**
     * 趋势图数据（支持日/周/月聚合，返回收入和支出）
     *
     * @param mode day / week / month
     */
    List<Map<String, Object>> trend(Long userId, String mode, LocalDate start, LocalDate end);

    /**
     * 获取财务设置
     */
    UserFinanceSetting getSetting(Long userId);

    /**
     * 更新财务设置
     */
    void updateSetting(UserFinanceSetting setting);
}
