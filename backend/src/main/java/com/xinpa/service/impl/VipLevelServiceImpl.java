package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.VipLevelMapper;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public List<VipLevel> listByUser(Long userId) {
        return vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getUserId, userId)
                        .orderByAsc(VipLevel::getLevel));
    }

    @Override
    public List<VipLevel> listEnabled(Long userId) {
        return vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getStatus, 1)
                        .eq(VipLevel::getUserId, userId)
                        .orderByAsc(VipLevel::getLevel));
    }

    @Override
    public VipLevel getById(Long id) {
        return vipLevelMapper.selectById(id);
    }

    @Override
    public void create(VipLevel vipLevel, Long userId) {
        if (vipLevel.getStatus() == null) {
            vipLevel.setStatus(1);
        }
        vipLevel.setUserId(userId);
        // 检查该用户下 level 是否已存在
        VipLevel exist = vipLevelMapper.selectOne(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getUserId, userId)
                        .eq(VipLevel::getLevel, vipLevel.getLevel()));
        if (exist != null) {
            throw new BusinessException("等级 " + vipLevel.getLevel() + " 已存在");
        }
        vipLevelMapper.insert(vipLevel);
    }

    @Override
    public void update(VipLevel vipLevel, Long userId) {
        vipLevelMapper.update(vipLevel,
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getId, vipLevel.getId())
                        .eq(VipLevel::getUserId, userId));
    }

    @Override
    public void delete(Long id, Long userId) {
        vipLevelMapper.delete(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getId, id)
                        .eq(VipLevel::getUserId, userId));
    }

    @Override
    public void initDefaults(Long userId) {
        List<VipLevel> exist = vipLevelMapper.selectList(
                new LambdaQueryWrapper<VipLevel>()
                        .eq(VipLevel::getDeleted, 0)
                        .eq(VipLevel::getUserId, userId));
        if (!exist.isEmpty()) return;

        Object[][] defaults = {
                {1, "VIP1", new BigDecimal("500"), 98, "全场98折", 1},
                {2, "VIP2", new BigDecimal("1500"), 98, "全场98折", 2},
                {3, "VIP3", new BigDecimal("3888"), 95, "全场95折", 3},
                {4, "VIP4", new BigDecimal("10000"), 95, "全场95折", 4},
                {5, "VIP5", new BigDecimal("16888"), 92, "全场92折", 5},
                {6, "VIP6", new BigDecimal("28888"), 92, "专属客服·全场9折", 6},
        };
        for (Object[] d : defaults) {
            VipLevel v = new VipLevel();
            v.setUserId(userId);
            v.setLevel((Integer) d[0]);
            v.setName((String) d[1]);
            v.setThreshold((BigDecimal) d[2]);
            v.setDiscount((Integer) d[3]);
            v.setBenefits((String) d[4]);
            v.setSortOrder((Integer) d[5]);
            v.setStatus(1);
            vipLevelMapper.insert(v);
        }
    }
}
