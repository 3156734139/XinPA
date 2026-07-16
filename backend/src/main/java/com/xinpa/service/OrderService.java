package com.xinpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.dto.OrderTimerDTO;
import com.xinpa.entity.Order;
import com.xinpa.entity.OrderTimerLog;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 分页查询订单
     */
    Page<Order> page(OrderQueryDTO query);

    /**
     * 获取订单详情
     */
    Order getById(Long id);

    /**
     * 创建订单
     */
    void create(Order order);

    /**
     * 更新订单
     */
    void update(Order order);

    /**
     * 开始计时
     */
    void startTimer(OrderTimerDTO dto);

    /**
     * 暂停计时
     */
    void pauseTimer(OrderTimerDTO dto);

    /**
     * 结束计时
     */
    void endTimer(OrderTimerDTO dto);

    /**
     * 补时
     */
    void addExtraMinutes(OrderTimerDTO dto);

    /**
     * 结算订单
     */
    void settle(Long id, Long userId);

    /**
     * 售后退款
     */
    void refund(Long id, Long userId);

    /**
     * 获取当前用户各套餐的订单数和收入统计
     */
    List<Map<String, Object>> getPackageStats(Long userId);
}
