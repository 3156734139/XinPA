package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快捷文案实体
 */
@Data
@TableName("copywriting")
public class Copywriting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String category;
    private String title;
    private String content;
    private Integer isAiGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
