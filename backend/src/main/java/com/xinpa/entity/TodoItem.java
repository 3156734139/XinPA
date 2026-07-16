package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办看板实体
 */
@Data
@TableName("todo_item")
public class TodoItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    /** 1素材更新 2平台签到 3自定义 */
    private Integer todoType;
    /** 0待办 1完成 */
    private Integer status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
