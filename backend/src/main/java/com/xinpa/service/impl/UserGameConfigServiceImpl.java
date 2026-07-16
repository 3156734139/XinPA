package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.UserGameConfig;
import com.xinpa.mapper.UserGameConfigMapper;
import com.xinpa.service.UserGameConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户游戏配置服务实现
 */
@Service
@RequiredArgsConstructor
public class UserGameConfigServiceImpl implements UserGameConfigService {

    private final UserGameConfigMapper userGameConfigMapper;

    @Override
    public List<UserGameConfig> listByUserId(Long userId) {
        return userGameConfigMapper.selectList(
                new LambdaQueryWrapper<UserGameConfig>()
                        .eq(UserGameConfig::getUserId, userId)
                        .orderByAsc(UserGameConfig::getSortOrder)
                        .orderByAsc(UserGameConfig::getId));
    }

    @Override
    public UserGameConfig getById(Long id) {
        return userGameConfigMapper.selectById(id);
    }

    @Override
    public void add(UserGameConfig config) {
        userGameConfigMapper.insert(config);
    }

    @Override
    public void update(UserGameConfig config) {
        userGameConfigMapper.updateById(config);
    }

    @Override
    public void delete(Long id, Long userId) {
        userGameConfigMapper.delete(new LambdaQueryWrapper<UserGameConfig>()
                .eq(UserGameConfig::getId, id)
                .eq(UserGameConfig::getUserId, userId));
    }
}
