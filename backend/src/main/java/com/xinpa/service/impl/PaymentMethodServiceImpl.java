package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Order;
import com.xinpa.entity.PaymentMethod;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.mapper.PaymentMethodMapper;
import com.xinpa.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付方式服务实现
 */
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodMapper paymentMethodMapper;
    private final OrderMapper orderMapper;

    @Override
    public List<PaymentMethod> listAll() {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .orderByAsc(PaymentMethod::getSortOrder));
    }

    @Override
    public List<PaymentMethod> listByUser(Long userId) {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .eq(PaymentMethod::getUserId, userId)
                        .orderByAsc(PaymentMethod::getSortOrder));
    }

    @Override
    public List<PaymentMethod> listEnabled(Long userId) {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .eq(PaymentMethod::getStatus, 1)
                        .eq(PaymentMethod::getUserId, userId)
                        .orderByAsc(PaymentMethod::getSortOrder));
    }

    @Override
    public PaymentMethod getById(Long id) {
        return paymentMethodMapper.selectById(id);
    }

    @Override
    public void create(PaymentMethod paymentMethod, Long userId) {
        if (paymentMethod.getStatus() == null) {
            paymentMethod.setStatus(1);
        }
        paymentMethod.setUserId(userId);
        paymentMethodMapper.insert(paymentMethod);
    }

    @Override
    public void update(PaymentMethod paymentMethod, Long userId) {
        paymentMethodMapper.update(paymentMethod,
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getId, paymentMethod.getId())
                        .eq(PaymentMethod::getUserId, userId));
    }

    @Override
    public void delete(Long id, Long userId) {
        // 检查是否有订单在使用该支付方式
        long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getPaymentMethodId, id));
        if (orderCount > 0) {
            throw new BusinessException("该支付方式下有 " + orderCount + " 个订单正在使用，无法删除");
        }

        paymentMethodMapper.delete(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getId, id)
                        .eq(PaymentMethod::getUserId, userId));
    }

    @Override
    public void initDefaults(Long userId) {
        List<PaymentMethod> exist = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .eq(PaymentMethod::getUserId, userId));
        if (!exist.isEmpty()) return;

        String[][] defaults = {
                {"平台", "1"},
                {"微信", "2"},
                {"支付宝", "3"},
                {"现金", "4"},
        };
        for (String[] d : defaults) {
            PaymentMethod pm = new PaymentMethod();
            pm.setUserId(userId);
            pm.setName(d[0]);
            pm.setSortOrder(Integer.valueOf(d[1]));
            pm.setStatus(1);
            paymentMethodMapper.insert(pm);
        }
    }
}
