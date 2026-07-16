package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI调用日志实体
 */
@Data
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String scene;
    private String prompt;
    private String response;
    private Integer tokens;
    private Integer success;
    private String errorMsg;
    private LocalDateTime createdAt;
}
