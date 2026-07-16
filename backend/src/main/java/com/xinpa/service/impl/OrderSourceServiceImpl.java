package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.OrderSource;
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

    @Override
    public List<OrderSource> listAll() {
        return orderSourceMapper.selectList(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getDeleted, 0)
                        .orderByAsc(OrderSource::getSortOrder));
    }

    @Override
    public List<OrderSource> listEnabled() {
        return orderSourceMapper.selectList(
                new LambdaQueryWrapper<OrderSource>()
                        .eq(OrderSource::getDeleted, 0)
                        .eq(OrderSource::getStatus, 1)
                        .orderByAsc(OrderSource::getSortOrder));
    }

    @Override
    public OrderSource getById(Long id) {
        return orderSourceMapper.selectById(id);
    }

    @Override
    public void create(OrderSource orderSource) {
        if (orderSource.getStatus() == null) {
            orderSource.setStatus(1);
        }
        orderSourceMapper.insert(orderSource);
    }

    @Override
    public void update(OrderSource orderSource) {
        orderSourceMapper.updateById(orderSource);
    }

    @Override
    public void delete(Long id) {
        orderSourceMapper.deleteById(id);
    }
}
