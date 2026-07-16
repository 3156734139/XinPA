package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.UserProfile;
import com.xinpa.mapper.UserProfileMapper;
import com.xinpa.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 人设主页服务实现
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getByUserId(Long userId) {
        return userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
    }

    @Override
    public void update(UserProfile profile) {
        userProfileMapper.updateById(profile);
    }

    @Override
    public void updateOrderStatus(Long userId, Integer orderStatus) {
        UserProfile profile = getByUserId(userId);
        if (profile != null) {
            profile.setOrderStatus(orderStatus);
            userProfileMapper.updateById(profile);
        }
    }
}
