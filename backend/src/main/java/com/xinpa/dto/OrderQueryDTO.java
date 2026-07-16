package com.xinpa.dto;

import lombok.Data;

/**
 * 订单查询参数
 */
@Data
public class OrderQueryDTO {

    private Long userId;
    private Integer status;
    private Integer orderSource;
    private String keyword;
    private long current = 1;
    private long size = 20;
}
