package com.xinpa.service;

import java.util.List;
import java.util.Map;

/**
 * 平台统计服务接口
 */
public interface StatsService {

    /**
     * 平台总览数据
     */
    Map<String, Object> getOverview();

    /**
     * 用户增长曲线（近30天）
     */
    List<Map<String, Object>> getUserGrowth();

    /**
     * 各品类陪玩占比
     */
    List<Map<String, Object>> getTemplateTypeRatio();

    /**
     * 套餐类型分布
     */
    List<Map<String, Object>> getPackageTypeRatio();
}
