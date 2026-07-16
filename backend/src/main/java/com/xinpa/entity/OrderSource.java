package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单来源字典（全局，管理员维护）
 */
@Data
@TableName("order_source")
public class OrderSource {

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
