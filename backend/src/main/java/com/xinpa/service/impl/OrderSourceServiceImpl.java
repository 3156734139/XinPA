package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Customer;
import com.xinpa.entity.Order;
import com.xinpa.entity.OrderSource;
import com.xinpa.mapper.CustomerMapper;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.mapper.OrderSourceMapper;
import com.xinpa.service.OrderSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单来源服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderSourceServiceImpl implements OrderSourceService {

    private final OrderSourceMapper orderSourceMapper;
    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<OrderSource> listEnabled(Long userId) {
        return orderSourceMapper.selectList(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getDeleted, 0)
                        .eq(OrderSource::getStatus, 1)
                        .eq(OrderSource::getUserId, userId)
                        .orderByAsc(OrderSource::getSortOrder));
    }

    @Override
    public List<OrderSource> listByUser(Long userId) {
        return orderSourceMapper.selectList(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getDeleted, 0)
                        .eq(OrderSource::getUserId, userId)
                        .orderByAsc(OrderSource::getSortOrder));
    }

    @Override
    public OrderSource getById(Long id) {
        return orderSourceMapper.selectById(id);
    }

    @Override
    public void create(OrderSource orderSource, Long userId) {
        if (orderSource.getStatus() == null) {
            orderSource.setStatus(1);
        }
        orderSource.setUserId(userId);
        orderSourceMapper.insert(orderSource);
    }

    @Override
    public void update(OrderSource orderSource, Long userId) {
        orderSourceMapper.update(orderSource,
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getId, orderSource.getId())
                        .eq(OrderSource::getUserId, userId));
    }

    @Override
    public void delete(Long id, Long userId) {
        // 检查是否有订单在使用该来源
        long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getOrderSource, id));
        if (orderCount > 0) {
            throw new BusinessException("该来源下有 " + orderCount + " 个订单正在使用，无法删除");
        }

        // 检查是否有客户在使用该来源
        long customerCount = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getDeleted, 0)
                        .eq(Customer::getUserId, userId)
                        .eq(Customer::getSourceId, id));
        if (customerCount > 0) {
            throw new BusinessException("该来源下有 " + customerCount + " 个客户正在使用，无法删除");
        }

        orderSourceMapper.delete(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getId, id)
                        .eq(OrderSource::getUserId, userId));
    }

    @Override
    public void initDefaults(Long userId) {
        List<OrderSource> exist = orderSourceMapper.selectList(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getDeleted, 0)
                        .eq(OrderSource::getUserId, userId));
        if (!exist.isEmpty()) return;

        String[][] defaults = {
                {"pw店", "1"},
                {"抖音", "2"},
                {"小红书", "3"},
                {"其他", "4"},
        };
        for (String[] d : defaults) {
            OrderSource s = new OrderSource();
            s.setUserId(userId);
            s.setName(d[0]);
            s.setSortOrder(Integer.valueOf(d[1]));
            s.setStatus(1);
            orderSourceMapper.insert(s);
        }
    }
}
