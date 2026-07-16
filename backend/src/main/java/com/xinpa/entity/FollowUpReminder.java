package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回访提醒实体
 */
@Data
@TableName("follow_up_reminder")
public class FollowUpReminder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long customerId;
    private Long orderId;
    /** 1下单3天 2下单7天 3生日 4版本更新 */
    private Integer remindType;
    private LocalDateTime remindTime;
    private String aiGreeting;
    /** 0待处理 1已处理 2已忽略 */
    private Integer status;
    private LocalDateTime createdAt;
}
