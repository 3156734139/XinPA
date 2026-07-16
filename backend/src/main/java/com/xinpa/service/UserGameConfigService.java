package com.xinpa.service;

import com.xinpa.entity.UserGameConfig;

import java.util.List;

/**
 * 用户游戏配置服务接口
 */
public interface UserGameConfigService {

    List<UserGameConfig> listByUserId(Long userId);

    UserGameConfig getById(Long id);

    void add(UserGameConfig config);

    void update(UserGameConfig config);

    void delete(Long id, Long userId);
}
