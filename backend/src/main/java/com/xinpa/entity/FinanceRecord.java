package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
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
    private Integer recordType;
    private String category;
    private BigDecimal amount;
    private String paymentMethod;
    private BigDecimal platformFee;
    private java.time.LocalDate recordDate;
    private String remark;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
