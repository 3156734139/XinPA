package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Order;
import com.xinpa.entity.PricePackage;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.mapper.PricePackageMapper;
import com.xinpa.service.PricePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 价目套餐服务实现
 */
@Service
@RequiredArgsConstructor
public class PricePackageServiceImpl implements PricePackageService {

    private final PricePackageMapper pricePackageMapper;
    private final OrderMapper orderMapper;

    @Override
    public List<PricePackage> listByUserId(Long userId) {
        return pricePackageMapper.selectList(
                new LambdaQueryWrapper<PricePackage>()
                        .eq(PricePackage::getUserId, userId)
                        .eq(PricePackage::getDeleted, 0)
                        .orderByAsc(PricePackage::getName));
    }

    @Override
    public PricePackage getById(Long id) {
        return pricePackageMapper.selectById(id);
    }

    @Override
    public void add(PricePackage pricePackage) {
        pricePackageMapper.insert(pricePackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PricePackage pricePackage) {
        PricePackage existing = pricePackageMapper.selectById(pricePackage.getId());
        if (existing == null) throw new BusinessException("套餐不存在");
        if (!existing.getUserId().equals(pricePackage.getUserId())) {
            throw new BusinessException("无权操作该套餐");
        }
        // 同步更新关联订单的套餐名称
        if (pricePackage.getName() != null && !pricePackage.getName().equals(existing.getName())) {
            orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                    .eq(Order::getPackageId, pricePackage.getId())
                    .set(Order::getPackageName, pricePackage.getName()));
        }
        pricePackageMapper.updateById(pricePackage);
    }

    @Override
    public void delete(Long id, Long userId) {
        PricePackage pkg = pricePackageMapper.selectById(id);
        if (pkg == null || !pkg.getUserId().equals(userId)) {
            throw new BusinessException("套餐不存在");
        }
        pricePackageMapper.deleteById(id);
    }
}
