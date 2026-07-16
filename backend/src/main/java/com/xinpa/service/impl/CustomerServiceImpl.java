package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Customer;
import com.xinpa.mapper.CustomerMapper;
import com.xinpa.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户档案服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

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
    public Page<Customer> page(Long userId, String nickname, String contact, String source,
                               Integer spendLevel, BigDecimal minSpend, BigDecimal maxSpend,
                               Integer minOrders, Integer maxOrders, Integer isBlacklist,
                               long current, long size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getUserId, userId)
                .eq(Customer::getDeleted, 0)
                .eq(isBlacklist != null, Customer::getIsBlacklist, isBlacklist)
                .like(nickname != null && !nickname.isEmpty(), Customer::getNickname, nickname)
                .like(contact != null && !contact.isEmpty(), Customer::getContact, contact)
                .like(source != null && !source.isEmpty(), Customer::getSource, source)
                .eq(spendLevel != null, Customer::getSpendLevel, spendLevel)
                .ge(minSpend != null, Customer::getTotalSpend, minSpend)
                .le(maxSpend != null, Customer::getTotalSpend, maxSpend)
                .ge(minOrders != null, Customer::getOrderCount, minOrders)
                .le(maxOrders != null, Customer::getOrderCount, maxOrders)
                .orderByDesc(Customer::getCreatedAt);
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
}
