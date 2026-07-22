package com.xinpa.service;

import com.xinpa.entity.OrderSource;

import java.util.List;

/**
 * 订单来源服务接口
 */
public interface OrderSourceService {

    /** 用户的已启用来源列表（订单下拉选单用） */
    List<OrderSource> listEnabled(Long userId);

    /** 用户的来源列表（管理用，含禁用） */
    List<OrderSource> listByUser(Long userId);

    OrderSource getById(Long id);

    /** 为用户创建来源 */
    void create(OrderSource orderSource, Long userId);

    /** 更新并校验归属 */
    void update(OrderSource orderSource, Long userId);

    /** 删除并校验归属 */
    void delete(Long id, Long userId);

    /** 为用户批量初始化默认来源 */
    void initDefaults(Long userId);
}
