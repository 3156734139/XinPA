package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务流水实体
 */
@Data
@TableName("finance_record")
public class FinanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    /** 1收入 2支出 */
    @NotNull(message = "流水类型不能为空")
    private Integer recordType;
    private String category;
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
    private String paymentMethod;
    private BigDecimal platformFee;
    @NotNull(message = "日期不能为空")
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
