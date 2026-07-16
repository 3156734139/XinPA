package com.xinpa.service;

import com.xinpa.entity.SysAdmin;

/**
 * 管理员服务接口
 */
public interface SysAdminService {

    /**
     * 根据用户名查询
     */
    SysAdmin getByUsername(String username);

    /**
     * 根据ID查询
     */
    SysAdmin getById(Long id);

    /**
     * 更新最后登录时间
     */
    void updateLastLogin(Long id);
}
