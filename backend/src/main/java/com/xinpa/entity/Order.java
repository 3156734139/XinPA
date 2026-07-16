package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long customerId;
    private String orderNo;
    /** 1平台派单 2微信QQ私域 3线下预约 */
    private Integer orderSource;
    private Long packageId;
    private String title;
    /** 1待接单 2进行中 3待结算 4已完结 5售后退款 */
    private Integer status;
    private BigDecimal unitPrice;
    private BigDecimal plannedHours;
    private Integer actualMinutes;
    private Integer extraMinutes;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Integer isOvernight;
    private Integer isOffline;
    private Long couponId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime settleTime;
    private LocalDateTime appointmentTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
