package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单来源字典
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
    /** 所属用户ID */
    private Long userId;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
