package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户游戏配置 - 每个游戏独立的人设介绍
 */
@Data
@TableName("user_game_config")
public class UserGameConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String gameName;
    private String intro;
    private String openingLine;
    private String tags;
    private String rankInfo;
    private String positionInfo;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
