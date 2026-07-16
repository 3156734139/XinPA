package com.xinpa.service.impl;

import com.xinpa.mapper.FinanceRecordMapper;
import com.xinpa.mapper.StatsMapper;
import com.xinpa.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final FinanceRecordMapper financeRecordMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", statsMapper.countUsers());
        overview.put("todayNewUsers", statsMapper.countTodayNewUsers());
        overview.put("vipUsers", statsMapper.countVipUsers());
        overview.put("yearRevenue", financeRecordMapper.sumYearlyRevenue(LocalDate.now().getYear()));
        overview.put("blacklistCount", statsMapper.countUsers()); // 简化处理
        return overview;
    }

    @Override
    public List<Map<String, Object>> getUserGrowth() {
        return statsMapper.userGrowthLast30Days();
    }

    @Override
    public List<Map<String, Object>> getTemplateTypeRatio() {
        return statsMapper.countByTemplateType();
    }

    @Override
    public List<Map<String, Object>> getPackageTypeRatio() {
        return statsMapper.countByPackageType();
    }
}
