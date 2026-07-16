package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long customerId;
    private String name;
    /** 1时长折扣 2免费体验 */
    private Integer couponType;
    private BigDecimal discountValue;
    private BigDecimal freeHours;
    private BigDecimal minAmount;
    private LocalDateTime expireTime;
    /** 0已用 1未用 2过期 */
    private Integer status;
    private Long usedOrderId;
    private LocalDateTime createdAt;
}
