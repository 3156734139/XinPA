package com.xinpa.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 管理员统计专用 Mapper（仅聚合数据，不含用户隐私明细）
 */
@Mapper
public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0")
    long countUsers();

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND DATE(created_at) = CURDATE()")
    long countTodayNewUsers();

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND member_type = 1")
    long countVipUsers();

    @Select("SELECT template_type, COUNT(*) as count FROM user_profile GROUP BY template_type")
    List<Map<String, Object>> countByTemplateType();

    @Select("SELECT package_type, COUNT(*) as count FROM price_package WHERE deleted = 0 GROUP BY package_type")
    List<Map<String, Object>> countByPackageType();

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM sys_user WHERE deleted = 0 AND created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> userGrowthLast30Days();
}
