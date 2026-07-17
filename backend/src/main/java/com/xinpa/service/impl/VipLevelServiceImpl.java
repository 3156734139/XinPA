package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.VipLevelMapper;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * VIP等级配置服务实现
 */
@Service
@RequiredArgsConstructor
public class VipLevelServiceImpl implements VipLevelService {

    private final VipLevelMapper vipLevelMapper;

    @Override
    public List<VipLevel> listAll() {
        return vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .orderByAsc(VipLevel::getLevel));
    }

    @Override
    public List<VipLevel> listEnabled() {
        return vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getStatus, 1)
                        .orderByAsc(VipLevel::getLevel));
    }

    @Override
    public VipLevel getById(Long id) {
        return vipLevelMapper.selectById(id);
    }

    @Override
    public void create(VipLevel vipLevel) {
        if (vipLevel.getStatus() == null) {
            vipLevel.setStatus(1);
        }
        // 检查 level 是否已存在
        VipLevel exist = vipLevelMapper.selectOne(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getLevel, vipLevel.getLevel()));
        if (exist != null) {
            throw new BusinessException("等级 " + vipLevel.getLevel() + " 已存在");
        }
        vipLevelMapper.insert(vipLevel);
    }

    @Override
    public void update(VipLevel vipLevel) {
        vipLevelMapper.updateById(vipLevel);
    }

    @Override
    public void delete(Long id) {
        vipLevelMapper.deleteById(id);
    }
}
