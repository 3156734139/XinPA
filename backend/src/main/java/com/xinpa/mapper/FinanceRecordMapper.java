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

    /** 按日统计收入 */
    @Select("SELECT record_date as date, SUM(amount) as amount FROM finance_record WHERE user_id = #{userId} AND record_type = 1 AND record_date BETWEEN #{start} AND #{end} AND deleted = 0 GROUP BY record_date ORDER BY record_date")
    List<Map<String, Object>> dailyIncome(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 平台年度营收（管理员聚合） */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM finance_record WHERE record_type = 1 AND YEAR(record_date) = #{year} AND deleted = 0")
    BigDecimal sumYearlyRevenue(@Param("year") int year);
}
