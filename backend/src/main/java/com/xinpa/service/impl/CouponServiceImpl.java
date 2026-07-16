package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Coupon;
import com.xinpa.mapper.CouponMapper;
import com.xinpa.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优惠券服务实现
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;

    @Override
    public List<Coupon> listByUserId(Long userId, Long customerId) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getUserId, userId)
                .eq(customerId != null, Coupon::getCustomerId, customerId)
                .orderByDesc(Coupon::getCreatedAt);
        return couponMapper.selectList(wrapper);
    }

    @Override
    public void create(Coupon coupon) {
        couponMapper.insert(coupon);
    }

    @Override
    public void cancel(Long id, Long userId) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || !coupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        coupon.setStatus(2); // 过期
        couponMapper.updateById(coupon);
    }
}
