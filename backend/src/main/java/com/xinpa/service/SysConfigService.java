package com.xinpa.service;

import com.xinpa.entity.SysConfig;

import java.math.BigDecimal;
import java.util.List;

public interface SysConfigService {

    List<SysConfig> listAll();

    void create(SysConfig config);

    void update(SysConfig config);

    void delete(Long id);

    /** 获取指定VIP等级的消费门槛 */
    BigDecimal getVipThreshold(int vipLevel);

    /** 获取指定VIP等级的折扣（如 98 = 9.8折） */
    int getVipDiscount(int vipLevel);
}
