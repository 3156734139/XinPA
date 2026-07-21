package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 素材库实体
 */
@Data
@TableName("material")
public class Material {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private MaterialType materialType;
    private String fileUrl;
    private Long fileSize;
    private Integer watermark;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
