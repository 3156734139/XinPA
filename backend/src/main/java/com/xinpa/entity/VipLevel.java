package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP等级配置（管理员维护）
 */
@Data
@TableName("vip_level")
public class VipLevel {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer level;
    private String name;
    private BigDecimal threshold;
    private Integer discount;
    /** 等级福利描述 */
    private String benefits;
    private Integer sortOrder;
    /** 0禁用 1启用 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
