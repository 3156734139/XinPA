package com.xinpa.service;

import com.xinpa.entity.PricePackage;

import java.util.List;

/**
 * 价目套餐服务接口
 */
public interface PricePackageService {

    /**
     * 获取用户所有套餐
     */
    List<PricePackage> listByUserId(Long userId);

    /**
     * 获取套餐详情
     */
    PricePackage getById(Long id);

    /**
     * 添加套餐
     */
    void add(PricePackage pricePackage);

    /**
     * 更新套餐
     */
    void update(PricePackage pricePackage);

    /**
     * 删除套餐
     */
    void delete(Long id, Long userId);
}
