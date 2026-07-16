package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.mapper.FinanceRecordMapper;
import com.xinpa.mapper.UserFinanceSettingMapper;
import com.xinpa.service.FinanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务服务实现
 */
@Service
@RequiredArgsConstructor
public class FinanceRecordServiceImpl implements FinanceRecordService {

    private final FinanceRecordMapper financeRecordMapper;
    private final UserFinanceSettingMapper userFinanceSettingMapper;

    @Override
    public Page<FinanceRecord> page(Long userId, Integer recordType, LocalDate start, LocalDate end, long current, long size) {
        LambdaQueryWrapper<FinanceRecord> wrapper = new LambdaQueryWrapper<FinanceRecord>()
                .eq(FinanceRecord::getUserId, userId)
                .eq(FinanceRecord::getDeleted, 0)
                .eq(recordType != null, FinanceRecord::getRecordType, recordType)
                .ge(start != null, FinanceRecord::getRecordDate, start)
                .le(end != null, FinanceRecord::getRecordDate, end)
                .orderByDesc(FinanceRecord::getRecordDate);
        return financeRecordMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public void create(FinanceRecord record) {
        financeRecordMapper.insert(record);
    }

    @Override
    public void delete(Long id, Long userId) {
        FinanceRecord record = financeRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("流水不存在");
        }
        financeRecordMapper.deleteById(id);
    }

    @Override
    public BigDecimal sumIncome(Long userId, LocalDate start, LocalDate end) {
        return financeRecordMapper.sumByTypeAndDateRange(userId, 1, start, end);
    }

    @Override
    public BigDecimal sumExpense(Long userId, LocalDate start, LocalDate end) {
        return financeRecordMapper.sumByTypeAndDateRange(userId, 2, start, end);
    }

    @Override
    public List<Map<String, Object>> dailyIncome(Long userId, LocalDate start, LocalDate end) {
        return financeRecordMapper.dailyIncome(userId, start, end);
    }

    @Override
    public UserFinanceSetting getSetting(Long userId) {
        UserFinanceSetting setting = userFinanceSettingMapper.selectOne(
                new LambdaQueryWrapper<UserFinanceSetting>()
                        .eq(UserFinanceSetting::getUserId, userId));
        if (setting == null) {
            setting = new UserFinanceSetting();
            setting.setUserId(userId);
            setting.setMonthlyTarget(BigDecimal.ZERO);
            setting.setWithdrawFeeRate(BigDecimal.valueOf(0.006));
            userFinanceSettingMapper.insert(setting);
        }
        return setting;
    }

    @Override
    public void updateSetting(UserFinanceSetting setting) {
        userFinanceSettingMapper.updateById(setting);
    }
}
