package com.xinpa.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录返回
 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;
    private String refreshToken;
    private String nickname;
    private Long userId;
    private Integer memberType;
}
