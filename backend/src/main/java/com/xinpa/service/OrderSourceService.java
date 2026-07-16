package com.xinpa.service;

import com.xinpa.entity.OrderSource;

import java.util.List;

/**
 * 订单来源服务接口
 */
public interface OrderSourceService {

    List<OrderSource> listAll();

    List<OrderSource> listEnabled();

    OrderSource getById(Long id);

    void create(OrderSource orderSource);

    void update(OrderSource orderSource);

    void delete(Long id);
}
