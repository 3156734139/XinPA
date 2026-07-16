package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.PricePackage;
import com.xinpa.mapper.PricePackageMapper;
import com.xinpa.service.PricePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 价目套餐服务实现
 */
@Service
@RequiredArgsConstructor
public class PricePackageServiceImpl implements PricePackageService {

    private final PricePackageMapper pricePackageMapper;

    @Override
    public List<PricePackage> listByUserId(Long userId) {
        return pricePackageMapper.selectList(
                new LambdaQueryWrapper<PricePackage>()
                        .eq(PricePackage::getUserId, userId)
                        .eq(PricePackage::getDeleted, 0)
                        .orderByAsc(PricePackage::getSortOrder));
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
    public void update(PricePackage pricePackage) {
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
