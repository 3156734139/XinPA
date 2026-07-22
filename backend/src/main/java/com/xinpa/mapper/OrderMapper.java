package com.xinpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinpa.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /** 统计用户订单数 */
    @Select("SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} AND deleted = 0")
    long countByUserId(@Param("userId") Long userId);

    /** 平台全局订单统计（管理员用，不含用户明细） */
    @Select("SELECT COUNT(*) FROM `order` WHERE deleted = 0")
    long countAll();

    @Select("SELECT COUNT(*) FROM `order` WHERE deleted = 0 AND DATE(created_at) = #{date}")
    long countByDate(@Param("date") LocalDate date);

    /** 订单时段分布 */
    @Select("SELECT HOUR(created_at) as hour, COUNT(*) as count FROM `order` WHERE deleted = 0 GROUP BY HOUR(created_at)")
    List<Map<String, Object>> countByHour();

    /** 用户营收统计 */
    @Select("SELECT COALESCE(SUM(final_amount), 0) FROM `order` WHERE user_id = #{userId} AND status = 4 AND deleted = 0")
    BigDecimal sumRevenueByUser(@Param("userId") Long userId);

    /** 套餐统计：每个套餐的订单数和收入 */
    @Select("SELECT package_id, COUNT(*) as order_count, COALESCE(SUM(final_amount), 0) as total_revenue " +
            "FROM `order` WHERE user_id = #{userId} AND package_id IS NOT NULL AND deleted = 0 " +
            "GROUP BY package_id")
    List<Map<String, Object>> selectPackageStats(@Param("userId") Long userId);

    /** 用户今日订单数 */
    @Select("SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} AND deleted = 0 AND DATE(created_at) = #{date}")
    long countTodayByUserId(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 客户订单统计（已完成订单） */
    @Select("SELECT COUNT(*) as order_count, COALESCE(SUM(final_amount), 0) as total_spend, " +
            "MIN(start_time) as first_order_time " +
            "FROM `order` WHERE customer_id = #{customerId} AND status = 4 AND deleted = 0")
    Map<String, Object> selectCustomerStats(@Param("customerId") Long customerId);
}
