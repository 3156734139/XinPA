package com.xinpa.service;

import com.xinpa.entity.PaymentMethod;

import java.util.List;

/**
 * 支付方式服务接口
 */
public interface PaymentMethodService {

    List<PaymentMethod> listAll();

    List<PaymentMethod> listEnabled();

    PaymentMethod getById(Long id);

    void create(PaymentMethod paymentMethod);

    void update(PaymentMethod paymentMethod);

    void delete(Long id);
}
