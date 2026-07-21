package com.xinpa.service;

import com.xinpa.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 根据手机号查询
     */
    SysUser getByPhone(String phone);

    /**
     * 根据ID查询
     */
    SysUser getById(Long id);

    /**
     * 注册（手机号+验证码）
     */
    SysUser registerByPhone(String phone, String nickname, String password);

    /**
     * 更新最后登录时间
     */
    void updateLastLogin(Long userId);

    /**
     * 更新用户信息
     */
    void updateInfo(SysUser user);
}
