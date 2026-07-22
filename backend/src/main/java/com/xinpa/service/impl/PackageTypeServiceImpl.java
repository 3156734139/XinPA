package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.PackageType;
import com.xinpa.entity.PricePackage;
import com.xinpa.mapper.PackageTypeMapper;
import com.xinpa.mapper.PricePackageMapper;
import com.xinpa.service.PackageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 套餐类型服务实现
 */
@Service
@RequiredArgsConstructor
public class PackageTypeServiceImpl implements PackageTypeService {

    private final PackageTypeMapper packageTypeMapper;
    private final PricePackageMapper pricePackageMapper;

    @Override
    public List<PackageType> listAll() {
        return packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .orderByAsc(PackageType::getSortOrder));
    }

    @Override
    public List<PackageType> listByUser(Long userId) {
        return packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .eq(PackageType::getUserId, userId)
                        .orderByAsc(PackageType::getSortOrder));
    }

    @Override
    public List<PackageType> listEnabled(Long userId) {
        return packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .eq(PackageType::getStatus, 1)
                        .eq(PackageType::getUserId, userId)
                        .orderByAsc(PackageType::getSortOrder));
    }

    @Override
    public PackageType getById(Long id) {
        return packageTypeMapper.selectById(id);
    }

    @Override
    public void create(PackageType packageType, Long userId) {
        if (packageType.getStatus() == null) {
            packageType.setStatus(1);
        }
        packageType.setUserId(userId);
        packageTypeMapper.insert(packageType);
    }

    @Override
    public void update(PackageType packageType, Long userId) {
        packageTypeMapper.update(packageType,
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getId, packageType.getId())
                        .eq(PackageType::getUserId, userId));
    }

    @Override
    public void delete(Long id, Long userId) {
        PackageType pt = packageTypeMapper.selectById(id);
        if (pt == null) return;

        // 检查是否有套餐正在使用该类型
        long usedCount = pricePackageMapper.selectCount(
                new LambdaQueryWrapper<PricePackage>()
                        .eq(PricePackage::getDeleted, 0)
                        .eq(PricePackage::getPackageType, pt.getSortOrder())
                        .eq(PricePackage::getUserId, userId));
        if (usedCount > 0) {
            throw new BusinessException("该套餐类型下有 " + usedCount + " 个套餐正在使用，无法删除");
        }

        packageTypeMapper.delete(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getId, id)
                        .eq(PackageType::getUserId, userId));
    }

    @Override
    public void initDefaults(Long userId) {
        List<PackageType> exist = packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .eq(PackageType::getUserId, userId));
        if (!exist.isEmpty()) return;

        String[][] defaults = {
                {"小时单", "1"},
                {"包夜", "2"},
                {"教学", "3"},
                {"包月", "4"},
                {"线下", "5"},
        };
        for (String[] d : defaults) {
            PackageType pt = new PackageType();
            pt.setUserId(userId);
            pt.setName(d[0]);
            pt.setSortOrder(Integer.valueOf(d[1]));
            pt.setStatus(1);
            packageTypeMapper.insert(pt);
        }
    }
}
