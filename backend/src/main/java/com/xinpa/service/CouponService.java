package com.xinpa.service;

import com.xinpa.entity.Coupon;

import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 获取用户的所有优惠券
     */
    List<Coupon> listByUserId(Long userId, Long customerId);

    /**
     * 发放优惠券
     */
    void create(Coupon coupon);

    /**
     * 作废优惠券
     */
    void cancel(Long id, Long userId);
}
