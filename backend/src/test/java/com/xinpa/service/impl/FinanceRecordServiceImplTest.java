package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.mapper.FinanceRecordMapper;
import com.xinpa.mapper.UserFinanceSettingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FinanceRecordServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FinanceRecordServiceImplTest {

    @Mock
    private FinanceRecordMapper financeRecordMapper;
    @Mock
    private UserFinanceSettingMapper userFinanceSettingMapper;

    @Captor
    private ArgumentCaptor<UserFinanceSetting> settingCaptor;

    private FinanceRecordServiceImpl financeRecordService;

    @BeforeEach
    void setUp() {
        financeRecordService = new FinanceRecordServiceImpl(financeRecordMapper, userFinanceSettingMapper);
    }

    @Nested
    @DisplayName("获取财务设置 getSetting()")
    class GetSettingTest {

        @Test
        @DisplayName("已有设置时直接返回")
        void existingSetting() {
            UserFinanceSetting existing = new UserFinanceSetting();
            existing.setUserId(100L);
            existing.setMonthlyTarget(new BigDecimal("5000"));
            existing.setWithdrawFeeRate(new BigDecimal("0.01"));

            when(userFinanceSettingMapper.selectOne(any())).thenReturn(existing);

            UserFinanceSetting result = financeRecordService.getSetting(100L);

            assertEquals(new BigDecimal("5000"), result.getMonthlyTarget());
            assertEquals(new BigDecimal("0.01"), result.getWithdrawFeeRate());
            verify(userFinanceSettingMapper, never()).insert(any());
        }

        @Test
        @DisplayName("无设置时创建默认值")
        void noSetting_createsDefault() {
            when(userFinanceSettingMapper.selectOne(any())).thenReturn(null);

            UserFinanceSetting result = financeRecordService.getSetting(100L);

            verify(userFinanceSettingMapper).insert(settingCaptor.capture());
            UserFinanceSetting inserted = settingCaptor.getValue();
            assertEquals(100L, inserted.getUserId().longValue());
            assertEquals(BigDecimal.ZERO, inserted.getMonthlyTarget());
            assertEquals(0, new BigDecimal("0.006").compareTo(inserted.getWithdrawFeeRate()));

            assertEquals(100L, result.getUserId().longValue());
        }
    }

    @Nested
    @DisplayName("删除财务记录 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除")
        void success() {
            FinanceRecord record = new FinanceRecord();
            record.setId(1L);
            record.setUserId(100L);
            when(financeRecordMapper.selectById(1L)).thenReturn(record);

            financeRecordService.delete(1L, 100L);
            verify(financeRecordMapper).deleteById((Long) 1L);
        }

        @Test
        @DisplayName("用户ID不匹配时抛出异常")
        void userIdMismatch_shouldThrow() {
            FinanceRecord record = new FinanceRecord();
            record.setId(1L);
            record.setUserId(100L);
            when(financeRecordMapper.selectById(1L)).thenReturn(record);

            assertThrows(BusinessException.class, () -> financeRecordService.delete(1L, 999L));
            verify(financeRecordMapper, never()).deleteById(any(java.io.Serializable.class));
        }
    }

    @Nested
    @DisplayName("收支趋势 trend()")
    class TrendTest {

        @Test
        @DisplayName("按日模式路由到 dailyFinance")
        void dayMode() {
            LocalDate start = LocalDate.of(2026, 7, 1);
            LocalDate end = LocalDate.of(2026, 7, 17);
            Map<String, Object> item = new HashMap<>();
            item.put("date", "2026-07-01");
            item.put("income", "100");
            item.put("expense", "50");
            var mockResult = List.of(item);
            when(financeRecordMapper.dailyFinance(100L, start, end)).thenReturn(mockResult);

            List<Map<String, Object>> result = financeRecordService.trend(100L, "day", start, end);

            assertEquals(1, result.size());
            assertEquals("2026-07-01", result.get(0).get("date"));
        }

        @Test
        @DisplayName("按周模式路由到 weeklyFinance")
        void weekMode() {
            LocalDate start = LocalDate.of(2026, 7, 1);
            LocalDate end = LocalDate.of(2026, 7, 17);
            Map<String, Object> item = new HashMap<>();
            item.put("date", "2026-07-06");
            item.put("income", "500");
            item.put("expense", "200");
            var mockResult = List.of(item);
            when(financeRecordMapper.weeklyFinance(100L, start, end)).thenReturn(mockResult);

            List<Map<String, Object>> result = financeRecordService.trend(100L, "week", start, end);

            assertEquals(1, result.size());
            assertEquals("500", result.get(0).get("income").toString());
        }

        @Test
        @DisplayName("按月模式路由到 monthlyFinance")
        void monthMode() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 7, 17);
            Map<String, Object> item = new HashMap<>();
            item.put("date", "2026-01-01");
            item.put("income", "3000");
            item.put("expense", "1000");
            var mockResult = List.of(item);
            when(financeRecordMapper.monthlyFinance(100L, start, end)).thenReturn(mockResult);

            List<Map<String, Object>> result = financeRecordService.trend(100L, "month", start, end);

            assertEquals(1, result.size());
            assertEquals("3000", result.get(0).get("income").toString());
        }

        @Test
        @DisplayName("start/end为null时使用默认值（30天内）")
        void defaultDateRange() {
            when(financeRecordMapper.dailyFinance(anyLong(), any(), any())).thenReturn(List.of());

            List<Map<String, Object>> result = financeRecordService.trend(100L, "day", null, null);

            assertNotNull(result);
            // 验证start被设为30天前
            var captor = ArgumentCaptor.forClass(LocalDate.class);
            verify(financeRecordMapper).dailyFinance(anyLong(), captor.capture(), any());
            assertEquals(LocalDate.now().minusDays(30), captor.getValue());
        }
    }

    @Nested
    @DisplayName("财务统计")
    class StatsTest {

        @Test
        @DisplayName("收入汇总委托给Mapper")
        void sumIncome() {
            LocalDate start = LocalDate.of(2026, 7, 1);
            LocalDate end = LocalDate.of(2026, 7, 31);
            when(financeRecordMapper.sumByTypeAndDateRange(100L, 1, start, end))
                    .thenReturn(new BigDecimal("5000"));

            BigDecimal result = financeRecordService.sumIncome(100L, start, end);
            assertEquals(0, new BigDecimal("5000").compareTo(result));
        }

        @Test
        @DisplayName("支出汇总委托给Mapper")
        void sumExpense() {
            LocalDate start = LocalDate.of(2026, 7, 1);
            LocalDate end = LocalDate.of(2026, 7, 31);
            when(financeRecordMapper.sumByTypeAndDateRange(100L, 2, start, end))
                    .thenReturn(new BigDecimal("1000"));

            BigDecimal result = financeRecordService.sumExpense(100L, start, end);
            assertEquals(0, new BigDecimal("1000").compareTo(result));
        }
    }
}
