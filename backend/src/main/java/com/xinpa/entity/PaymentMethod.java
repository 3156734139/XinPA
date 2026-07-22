package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付方式字典（全局，管理员维护）
 */
@Data
@TableName("payment_method")
public class PaymentMethod {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sortOrder;
    /** 0禁用 1启用 */
    private Integer status;
    private Long userId;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
