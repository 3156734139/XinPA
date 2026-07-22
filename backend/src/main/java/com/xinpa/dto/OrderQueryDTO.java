package com.xinpa.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单查询参数
 */
@Data
public class OrderQueryDTO {

    private Long userId;
    private Integer orderSource;
    private String keyword;
    private Long customerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minMinutes;
    private Integer maxMinutes;
    private String packageName;
    private long current = 1;
    private long size = 20;
    /** 排序字段，如 startTime、finalAmount、createdAt */
    private String sortField;
    /** 排序方向：asc 或 desc */
    private String sortOrder;
    /** 按实际服务时间（startTime）范围过滤：起始日期 */
    private LocalDate startTimeStart;
    /** 按实际服务时间（startTime）范围过滤：结束日期 */
    private LocalDate startTimeEnd;
}
