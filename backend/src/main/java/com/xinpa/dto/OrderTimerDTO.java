package com.xinpa.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单计时操作参数
 */
@Data
public class OrderTimerDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 补时时长(分钟) */
    private Integer extraMinutes;
}
