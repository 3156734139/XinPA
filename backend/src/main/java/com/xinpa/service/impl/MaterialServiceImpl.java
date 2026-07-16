package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Material;
import com.xinpa.mapper.MaterialMapper;
import com.xinpa.service.MaterialService;
import com.xinpa.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 素材服务实现
 */
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;
    private final MinioUtil minioUtil;

    @Override
    public List<Material> listByUserId(Long userId, Integer materialType) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<Material>()
                .eq(Material::getUserId, userId)
                .eq(Material::getDeleted, 0);
        if (materialType != null) {
            wrapper.eq(Material::getMaterialType, materialType);
        }
        wrapper.orderByDesc(Material::getCreatedAt);
        return materialMapper.selectList(wrapper);
    }

    @Override
    public Material upload(Long userId, String name, Integer materialType, MultipartFile file, Integer watermark) {
        try {
            String prefix = "user/" + userId + "/" + materialType;
            String objectName = minioUtil.upload(prefix, file);

            Material material = new Material();
            material.setUserId(userId);
            material.setName(name);
            material.setMaterialType(materialType);
            material.setFileUrl(objectName);
            material.setFileSize(file.getSize());
            material.setWatermark(watermark != null ? watermark : 0);
            materialMapper.insert(material);
            return material;
        } catch (Exception e) {
            throw new BusinessException("素材上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id, Long userId) {
        Material material = materialMapper.selectById(id);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException("素材不存在");
        }
        materialMapper.deleteById(id);
    }
}
