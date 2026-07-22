package com.xinpa.service;

import com.xinpa.entity.VipLevel;

import java.util.List;

/**
 * VIP等级配置服务接口
 */
public interface VipLevelService {

    List<VipLevel> listAll();

    /** 用户的VIP等级列表（管理用，含禁用） */
    List<VipLevel> listByUser(Long userId);

    /** 用户的已启用VIP等级列表 */
    List<VipLevel> listEnabled(Long userId);

    VipLevel getById(Long id);

    /** 为用户创建VIP等级 */
    void create(VipLevel vipLevel, Long userId);

    /** 更新VIP等级并校验归属 */
    void update(VipLevel vipLevel, Long userId);

    /** 删除VIP等级并校验归属 */
    void delete(Long id, Long userId);

    /** 为用户初始化默认VIP等级 */
    void initDefaults(Long userId);
}
