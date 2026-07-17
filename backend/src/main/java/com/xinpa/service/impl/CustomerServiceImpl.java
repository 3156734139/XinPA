package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Customer;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.CustomerMapper;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.service.CustomerService;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 客户档案服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final VipLevelService vipLevelService;

    @Override
    public List<Customer> listByUserId(Long userId) {
        return customerMapper.selectList(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getDeleted, 0)
                        .eq(Customer::getIsBlacklist, 0)
                        .orderByDesc(Customer::getCreatedAt));
    }

    @Override
    public Page<Customer> page(Long userId, String keyword, Long sourceId,
                               Integer spendLevel, BigDecimal minSpend, BigDecimal maxSpend,
                               Integer minOrders, Integer maxOrders, Integer isBlacklist,
                               long current, long size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getUserId, userId)
                .eq(Customer::getDeleted, 0)
                .eq(isBlacklist != null, Customer::getIsBlacklist, isBlacklist)
                .eq(sourceId != null, Customer::getSourceId, sourceId)
                .eq(spendLevel != null, Customer::getSpendLevel, spendLevel)
                .ge(minSpend != null, Customer::getTotalSpend, minSpend)
                .le(maxSpend != null, Customer::getTotalSpend, maxSpend)
                .ge(minOrders != null, Customer::getOrderCount, minOrders)
                .le(maxOrders != null, Customer::getOrderCount, maxOrders)
                .orderByDesc(Customer::getCreatedAt);
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Customer::getNickname, kw)
                    .or().like(Customer::getContact, kw));
        }
        return customerMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public Customer getById(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public void create(Customer customer) {
        customer.setOrderCount(0);
        customer.setTotalSpend(java.math.BigDecimal.ZERO);
        customerMapper.insert(customer);
    }

    @Override
    public void update(Customer customer) {
        Customer existing = customerMapper.selectById(customer.getId());
        if (existing == null) throw new BusinessException("客户不存在");
        if (!existing.getUserId().equals(customer.getUserId())) {
            throw new BusinessException("无权操作该客户");
        }
        customerMapper.updateById(customer);
    }

    @Override
    public void delete(Long id, Long userId) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException("客户不存在");
        }
        customerMapper.deleteById(id);
    }

    @Override
    public void addBlacklist(Long id, Long userId, String reason) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException("客户不存在");
        }
        customer.setIsBlacklist(1);
        customer.setBlacklistReason(reason);
        customerMapper.updateById(customer);
    }

    @Override
    public void removeBlacklist(Long id, Long userId) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null || !customer.getUserId().equals(userId)) {
            throw new BusinessException("客户不存在");
        }
        customer.setIsBlacklist(0);
        customer.setBlacklistReason(null);
        customerMapper.updateById(customer);
    }

    @Override
    public void updateSpendLevel(Long id, int level) {
        customerMapper.update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers.<Customer>lambdaUpdate()
                .eq(Customer::getId, id)
                .set(Customer::getSpendLevel, level));
    }

    @Override
    public long countByUserId(Long userId) {
        return customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getDeleted, 0));
    }

    @Override
    public void refreshCustomerStats(Long customerId) {
        if (customerId == null) return;
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) return;

        Map<String, Object> stats = orderMapper.selectCustomerStats(customerId);
        long count = stats != null && stats.get("order_count") != null
                ? ((Number) stats.get("order_count")).longValue() : 0;
        BigDecimal total = stats != null && stats.get("total_spend") != null
                ? (BigDecimal) stats.get("total_spend") : BigDecimal.ZERO;

        customer.setOrderCount((int) count);
        customer.setTotalSpend(total);
        if (count > 0) {
            customer.setLastOrderTime(LocalDateTime.now());
        }

        // 根据VIP等级配置计算优惠等级
        int level = 0;
        List<VipLevel> vipLevels = vipLevelService.listEnabled();
        for (VipLevel vl : vipLevels) {
            if (total.compareTo(vl.getThreshold()) >= 0 && vl.getLevel() > level) {
                level = vl.getLevel();
            }
        }
        customer.setSpendLevel(level);

        customerMapper.updateById(customer);
    }
}
