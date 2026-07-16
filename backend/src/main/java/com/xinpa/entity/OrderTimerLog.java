package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单计时记录实体
 */
@Data
@TableName("order_timer_log")
public class OrderTimerLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    /** 1开始 2暂停 3继续 4结束 5补时 */
    private Integer action;
    private Integer minutes;
    private LocalDateTime createdAt;
}
