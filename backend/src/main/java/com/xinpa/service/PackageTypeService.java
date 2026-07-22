package com.xinpa.service;

import com.xinpa.entity.PackageType;

import java.util.List;

/**
 * 套餐类型服务接口
 */
public interface PackageTypeService {

    List<PackageType> listAll();

    /** 用户的套餐类型列表（管理用，含禁用） */
    List<PackageType> listByUser(Long userId);

    /** 用户的已启用套餐类型列表 */
    List<PackageType> listEnabled(Long userId);

    PackageType getById(Long id);

    /** 为用户创建套餐类型 */
    void create(PackageType packageType, Long userId);

    /** 更新套餐类型并校验归属 */
    void update(PackageType packageType, Long userId);

    /** 删除套餐类型并校验归属 */
    void delete(Long id, Long userId);

    /** 为用户初始化默认套餐类型 */
    void initDefaults(Long userId);
}
