package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.entity.Coupon;
import com.xinpa.mapper.CouponMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * CouponServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponMapper couponMapper;

    @Captor
    private ArgumentCaptor<Coupon> couponCaptor;

    private CouponServiceImpl couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponServiceImpl(couponMapper);
    }

    @Nested
    @DisplayName("作废优惠券 cancel()")
    class CancelTest {

        @Test
        @DisplayName("正常作废：状态变为2")
        void success() {
            Coupon coupon = new Coupon();
            coupon.setId(1L);
            coupon.setUserId(100L);
            coupon.setStatus(1); // 未用
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            couponService.cancel(1L, 100L);

            verify(couponMapper).updateById(couponCaptor.capture());
            assertEquals(2, couponCaptor.getValue().getStatus().intValue());
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void notFound_shouldThrow() {
            when(couponMapper.selectById(anyLong())).thenReturn(null);
            assertThrows(BusinessException.class, () -> couponService.cancel(999L, 100L));
        }

        @Test
        @DisplayName("用户ID不匹配时抛出异常")
        void userIdMismatch_shouldThrow() {
            Coupon coupon = new Coupon();
            coupon.setId(1L);
            coupon.setUserId(100L);
            when(couponMapper.selectById(1L)).thenReturn(coupon);

            assertThrows(BusinessException.class, () -> couponService.cancel(1L, 999L));
            verify(couponMapper, never()).updateById(any());
        }
    }
}
