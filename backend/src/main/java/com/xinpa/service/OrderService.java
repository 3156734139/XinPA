package com.xinpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    Page<Order> page(OrderQueryDTO query);

    Order getById(Long id);

    void create(Order order);

    void update(Order order);

    List<Map<String, Object>> getPackageStats(Long userId);
}
