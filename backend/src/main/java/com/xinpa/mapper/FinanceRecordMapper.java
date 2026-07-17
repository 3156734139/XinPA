package com.xinpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinpa.entity.FinanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface FinanceRecordMapper extends BaseMapper<FinanceRecord> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM finance_record WHERE user_id = #{userId} AND record_type = #{type} AND record_date BETWEEN #{start} AND #{end} AND deleted = 0")
    BigDecimal sumByTypeAndDateRange(@Param("userId") Long userId, @Param("type") int type,
                                     @Param("start") LocalDate start, @Param("end") LocalDate end);

    // ==================== 趋势图（日/周/月聚合） ====================

    /** 按日聚合收支 */
    @Select("SELECT record_date as date, " +
            "SUM(CASE WHEN record_type = 1 THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN record_type = 2 THEN amount ELSE 0 END) as expense " +
            "FROM finance_record WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{start} AND #{end} AND deleted = 0 " +
            "GROUP BY record_date ORDER BY record_date")
    List<Map<String, Object>> dailyFinance(@Param("userId") Long userId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    /** 按周聚合收支 */
    @Select("SELECT MIN(record_date) as date, " +
            "SUM(CASE WHEN record_type = 1 THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN record_type = 2 THEN amount ELSE 0 END) as expense " +
            "FROM finance_record WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{start} AND #{end} AND deleted = 0 " +
            "GROUP BY YEARWEEK(record_date) ORDER BY date")
    List<Map<String, Object>> weeklyFinance(@Param("userId") Long userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    /** 按月聚合收支 */
    @Select("SELECT MIN(record_date) as date, " +
            "SUM(CASE WHEN record_type = 1 THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN record_type = 2 THEN amount ELSE 0 END) as expense " +
            "FROM finance_record WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{start} AND #{end} AND deleted = 0 " +
            "GROUP BY DATE_FORMAT(record_date, '%Y-%m') ORDER BY date")
    List<Map<String, Object>> monthlyFinance(@Param("userId") Long userId,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    /** 平台年度营收（管理员聚合） */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM finance_record WHERE record_type = 1 AND YEAR(record_date) = #{year} AND deleted = 0")
    BigDecimal sumYearlyRevenue(@Param("year") int year);
}
