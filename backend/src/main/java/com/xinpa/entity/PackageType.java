package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 套餐类型字典（全局，管理员维护）
 */
@Data
@TableName("package_type")
public class PackageType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sortOrder;
    /** 0禁用 1启用 */
    private Integer status;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
