package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.SysConfig;
import com.xinpa.mapper.SysConfigMapper;
import com.xinpa.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public List<SysConfig> listAll() {
        return sysConfigMapper.selectList(null);
    }

    @Override
    public void create(SysConfig config) {
        sysConfigMapper.insert(config);
    }

    @Override
    public void update(SysConfig config) {
        sysConfigMapper.updateById(config);
    }

    @Override
    public void delete(Long id) {
        sysConfigMapper.deleteById(id);
    }

    @Override
    public BigDecimal getVipThreshold(int vipLevel) {
        SysConfig c = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "vip" + vipLevel + "_threshold"));
        return c != null ? new BigDecimal(c.getConfigValue()) : BigDecimal.ZERO;
    }

    @Override
    public int getVipDiscount(int vipLevel) {
        SysConfig c = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "vip" + vipLevel + "_discount"));
        return c != null ? Integer.parseInt(c.getConfigValue()) : 100;
    }
}
