package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "客户不能为空")
    private Long customerId;
    private String orderNo;
    /** 1pw店 2抖音 3小红书 4其他 */
    @NotNull(message = "来源不能为空")
    private Integer orderSource;
    @NotNull(message = "套餐不能为空")
    private Long packageId;
    /** 套餐名称（冗余字段，创建时从套餐自动填充） */
    private String packageName;
    private String title;
    /** 1待接单 2进行中 3待结算 4已完结 5售后退款 */
    private Integer status;
    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;
    /** 计价单位，创建时从套餐自动填充（如：小时、次、晚） */
    private String unit;
    /** 计费时长(分钟)，创建时根据实际时长按规则折算 */
    private Integer billedMinutes;
    private Integer actualMinutes;
    private Integer extraMinutes;
    /** 客户名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String customerName;
    /** 是否应用VIP优惠（非数据库字段，前端控制） */
    @TableField(exist = false)
    private Boolean applyVipDiscount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    /** 支付方式: 平台/微信/支付宝/现金 */
    private String paymentMethod;
    /** 关联payment_method.id */
    @NotNull(message = "支付方式不能为空")
    private Long paymentMethodId;
    /** 到手比例(100=100%) */
    @NotNull(message = "到手比例不能为空")
    private BigDecimal settleRatio;
    private Integer isOvernight;
    private Integer isOffline;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    private LocalDateTime settleTime;
    private LocalDateTime appointmentTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
