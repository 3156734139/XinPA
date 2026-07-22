package com.xinpa.service;

import com.xinpa.entity.PaymentMethod;

import java.util.List;

/**
 * 支付方式服务接口
 */
public interface PaymentMethodService {

    List<PaymentMethod> listAll();

    /** 用户的支付方式列表（管理用，含禁用） */
    List<PaymentMethod> listByUser(Long userId);

    /** 用户的已启用支付方式列表 */
    List<PaymentMethod> listEnabled(Long userId);

    PaymentMethod getById(Long id);

    /** 为用户创建支付方式 */
    void create(PaymentMethod paymentMethod, Long userId);

    /** 更新支付方式并校验归属 */
    void update(PaymentMethod paymentMethod, Long userId);

    /** 删除支付方式并校验归属 */
    void delete(Long id, Long userId);

    /** 为用户初始化默认支付方式 */
    void initDefaults(Long userId);
}
