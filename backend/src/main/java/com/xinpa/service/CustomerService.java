package com.xinpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.entity.Customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户档案服务接口
 */
public interface CustomerService {

    /**
     * 获取用户所有客户（下拉选择用）
     */
    List<Customer> listByUserId(Long userId);

    /**
     * 分页查询客户
     */
    Page<Customer> page(Long userId, String keyword, Long sourceId,
                        Integer spendLevel, BigDecimal minSpend, BigDecimal maxSpend,
                        Integer minOrders, Integer maxOrders, Integer isBlacklist,
                        long current, long size);

    /**
     * 获取客户详情
     */
    Customer getById(Long id);

    /**
     * 创建客户
     */
    void create(Customer customer);

    /**
     * 更新客户
     */
    void update(Customer customer);

    /**
     * 删除客户
     */
    void delete(Long id, Long userId);

    /**
     * 加入黑名单
     */
    void addBlacklist(Long id, Long userId, String reason);

    /**
     * 移出黑名单
     */
    void removeBlacklist(Long id, Long userId);

    /**
     * 统计用户客户总数
     */
    long countByUserId(Long userId);

    /**
     * 根据已完成订单重新计算客户的累计消费、下单次数、消费等级
     */
    void refreshCustomerStats(Long customerId);

    /**
     * 仅更新优惠等级（不触发全量统计重新计算）
     */
    void updateSpendLevel(Long id, int level);
}
