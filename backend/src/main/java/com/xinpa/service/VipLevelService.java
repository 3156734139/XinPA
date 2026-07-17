package com.xinpa.service;

import com.xinpa.entity.VipLevel;

import java.util.List;

/**
 * VIP等级配置服务接口
 */
public interface VipLevelService {

    List<VipLevel> listAll();

    List<VipLevel> listEnabled();

    VipLevel getById(Long id);

    void create(VipLevel vipLevel);

    void update(VipLevel vipLevel);

    void delete(Long id);
}
