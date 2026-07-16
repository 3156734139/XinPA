package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价目套餐实体
 */
@Data
@TableName("price_package")
public class PricePackage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    /** 1小时单 2包夜 3教学 4包月 5线下 */
    private Integer packageType;
    private BigDecimal price;
    private String unit;
    private String description;
    private String discountRules;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
