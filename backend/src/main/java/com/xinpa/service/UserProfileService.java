package com.xinpa.service;

import com.xinpa.entity.UserProfile;

/**
 * 人设主页服务接口
 */
public interface UserProfileService {

    /**
     * 获取当前用户主页
     */
    UserProfile getByUserId(Long userId);

    /**
     * 更新主页信息
     */
    void update(UserProfile profile);

    /**
     * 更新接单状态
     */
    void updateOrderStatus(Long userId, Integer orderStatus);
}
