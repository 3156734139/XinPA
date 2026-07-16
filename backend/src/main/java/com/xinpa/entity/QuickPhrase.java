package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快捷短语实体
 */
@Data
@TableName("quick_phrase")
public class QuickPhrase {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String phrase;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
