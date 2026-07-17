package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户财务设置实体
 */
@Data
@TableName("user_finance_setting")
public class UserFinanceSetting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal monthlyTarget;
    private BigDecimal monthlyExpenseTarget;
    private BigDecimal withdrawFeeRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
