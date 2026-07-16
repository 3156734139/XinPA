package com.xinpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户信息
 */
@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer memberType;
    private LocalDateTime memberExpire;
    private LocalDateTime lastLoginTime;
}
