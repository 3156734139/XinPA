package com.xinpa.service;

import com.xinpa.entity.PackageType;

import java.util.List;

/**
 * 套餐类型服务接口
 */
public interface PackageTypeService {

    List<PackageType> listAll();

    List<PackageType> listEnabled();

    PackageType getById(Long id);

    void create(PackageType packageType);

    void update(PackageType packageType);

    void delete(Long id);
}
