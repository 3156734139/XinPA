package com.xinpa.service;

import com.xinpa.entity.Material;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 素材服务接口
 */
public interface MaterialService {

    /**
     * 获取用户素材列表
     */
    List<Material> listByUserId(Long userId, Integer materialType);

    /**
     * 上传素材
     */
    Material upload(Long userId, String name, Integer materialType, MultipartFile file, Integer watermark);

    /**
     * 删除素材
     */
    void delete(Long id, Long userId);
}
