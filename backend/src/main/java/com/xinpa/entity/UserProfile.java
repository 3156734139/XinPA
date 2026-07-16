package com.xinpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人人设主页实体
 */
@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 1游戏陪玩 2声优树洞 3线下陪伴 */
    private Integer templateType;
    private String intro;
    private String openingLine;
    /** 1在线 2休息 3通宵 4仅熟客 */
    private Integer orderStatus;
    private String games;
    private String ranks;
    private String positions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
