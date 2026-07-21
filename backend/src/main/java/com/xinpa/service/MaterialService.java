package com.xinpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.entity.Material;
import com.xinpa.entity.MaterialType;

import java.util.List;

/**
 * 素材服务接口
 */
public interface MaterialService {

    /**
     * 获取用户素材列表
     */
    List<Material> listByUserId(Long userId, MaterialType materialType);

    /**
     * 分页查询用户素材
     */
    Page<Material> pageByUserId(Long userId, MaterialType materialType, long current, long size);

    /**
     * 保存上传完成的素材记录（前端直传成功后通知入库 / OSS 回调）
     */
    Material saveFromCallback(Long userId, String name, MaterialType materialType, String objectKey, Long fileSize, Integer watermark);

    /**
     * 根据 ID 获取素材
     */
    Material getById(Long id);

    /**
     * 删除素材
     */
    void delete(Long id, Long userId);
}
