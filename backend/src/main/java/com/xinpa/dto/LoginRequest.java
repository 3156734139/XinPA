package com.xinpa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求
 * 两种登录方式共用：验证码登录填 phone + code，密码登录填 phone + password
 */
@Data
public class LoginRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /** 验证码（验证码登录时必填） */
    private String code;

    /** 密码（密码登录时必填） */
    @Size(min = 6, max = 64, message = "密码长度6-64位")
    private String password;

    /** 登录方式: sms=验证码登录, password=密码登录 */
    @NotBlank(message = "登录方式不能为空")
    private String loginType;
}
