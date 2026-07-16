package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.PaymentMethod;
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

    @Override
    public List<PaymentMethod> listAll() {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .orderByAsc(PaymentMethod::getSortOrder));
    }

    @Override
    public List<PaymentMethod> listEnabled() {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getDeleted, 0)
                        .eq(PaymentMethod::getStatus, 1)
                        .orderByAsc(PaymentMethod::getSortOrder));
    }

    @Override
    public PaymentMethod getById(Long id) {
        return paymentMethodMapper.selectById(id);
    }

    @Override
    public void create(PaymentMethod paymentMethod) {
        if (paymentMethod.getStatus() == null) {
            paymentMethod.setStatus(1);
        }
        paymentMethodMapper.insert(paymentMethod);
    }

    @Override
    public void update(PaymentMethod paymentMethod) {
        paymentMethodMapper.updateById(paymentMethod);
    }

    @Override
    public void delete(Long id) {
        paymentMethodMapper.deleteById(id);
    }
}
