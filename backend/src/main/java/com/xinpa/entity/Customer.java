package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户档案实体
 */
@Data
@TableName("customer")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String nickname;
    private String contact;
    private String source;
    private Integer spendLevel;
    private String gamePreference;
    private String personality;
    private LocalDate birthday;
    private String tags;
    private Integer isBlacklist;
    private String blacklistReason;
    private LocalDateTime lastOrderTime;
    private BigDecimal totalSpend;
    private Integer orderCount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
