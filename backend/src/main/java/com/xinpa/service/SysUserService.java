package com.xinpa.service;

import com.xinpa.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);

    /**
     * 根据ID查询
     */
    SysUser getById(Long id);

    /**
     * 注册
     */
    void register(String username, String password, String nickname);

    /**
     * 更新最后登录时间
     */
    void updateLastLogin(Long userId);

    /**
     * 更新用户信息
     */
    void updateInfo(SysUser user);
}
