package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Material;
import com.xinpa.entity.MaterialType;
import com.xinpa.mapper.MaterialMapper;
import com.xinpa.service.MaterialService;
import com.xinpa.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 素材服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;
    private final OssUtil ossUtil;

    @Override
    public Page<Material> pageByUserId(Long userId, MaterialType materialType, long current, long size) {
        LambdaQueryWrapper<Material> wrapper = buildQueryWrapper(userId, materialType);
        return materialMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public List<Material> listByUserId(Long userId, MaterialType materialType) {
        return materialMapper.selectList(buildQueryWrapper(userId, materialType));
    }

    private LambdaQueryWrapper<Material> buildQueryWrapper(Long userId, MaterialType materialType) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<Material>()
                .eq(Material::getUserId, userId)
                .eq(Material::getDeleted, 0);
        if (materialType != null) {
            wrapper.eq(Material::getMaterialType, materialType);
        }
        return wrapper.orderByDesc(Material::getCreatedAt);
    }

    @Override
    public Material saveFromCallback(Long userId, String name, MaterialType materialType,
                                     String objectKey, Long fileSize, Integer watermark) {
        // 可选：验证 OSS 文件确实存在
        if (!ossUtil.doesObjectExist(objectKey)) {
            log.warn("OSS 文件不存在: {}", objectKey);
            // 不抛异常，允许前端先通知后延迟上传的情况
        }

        Material material = new Material();
        material.setUserId(userId);
        material.setName(name);
        material.setMaterialType(materialType);
        material.setFileUrl(objectKey);
        material.setFileSize(fileSize);
        material.setWatermark(watermark != null ? watermark : 0);
        materialMapper.insert(material);
        log.info("素材入库成功: id={}, objectKey={}", material.getId(), objectKey);
        return material;
    }

    @Override
    public Material getById(Long id) {
        return materialMapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        Material material = materialMapper.selectById(id);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException("素材不存在");
        }
        // 删除 OSS 文件
        ossUtil.delete(material.getFileUrl());
        // 逻辑删除数据库记录
        materialMapper.deleteById(id);
        log.info("素材删除成功: id={}, objectKey={}", id, material.getFileUrl());
    }
}
