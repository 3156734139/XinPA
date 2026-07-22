package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "套餐名称不能为空")
    private String name;
    /** 1小时单 2包夜 3教学 4包月 5线下 */
    @NotNull(message = "套餐类型不能为空")
    private Integer packageType;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @NotBlank(message = "计价单位不能为空")
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
