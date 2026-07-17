package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "客户昵称不能为空")
    private String nickname;
    private String contact;
    private String source;
    /** 关联order_source.id */
    private Long sourceId;
    /** 优惠等级: 0无 1VIP1 2VIP2 3VIP3 4VIP4 5VIP5 6VIP6 */
    private Integer spendLevel;
    /** VIP等级名称（非数据库字段） */
    @TableField(exist = false)
    private String spendLevelName;
    /** VIP等级折扣（非数据库字段） */
    @TableField(exist = false)
    private Integer spendLevelDiscount;
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
