package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全平台公告实体
 */
@Data
@TableName("sys_announcement")
public class SysAnnouncement {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long adminId;
    /** 0下线 1发布 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
