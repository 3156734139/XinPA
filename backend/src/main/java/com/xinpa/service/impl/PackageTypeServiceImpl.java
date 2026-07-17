package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.PackageType;
import com.xinpa.mapper.PackageTypeMapper;
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

    @Override
    public List<PackageType> listAll() {
        return packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .orderByAsc(PackageType::getSortOrder));
    }

    @Override
    public List<PackageType> listEnabled() {
        return packageTypeMapper.selectList(
                new LambdaQueryWrapper<PackageType>()
                        .eq(PackageType::getDeleted, 0)
                        .eq(PackageType::getStatus, 1)
                        .orderByAsc(PackageType::getSortOrder));
    }

    @Override
    public PackageType getById(Long id) {
        return packageTypeMapper.selectById(id);
    }

    @Override
    public void create(PackageType packageType) {
        if (packageType.getStatus() == null) {
            packageType.setStatus(1);
        }
        packageTypeMapper.insert(packageType);
    }

    @Override
    public void update(PackageType packageType) {
        packageTypeMapper.updateById(packageType);
    }

    @Override
    public void delete(Long id) {
        packageTypeMapper.deleteById(id);
    }
}
