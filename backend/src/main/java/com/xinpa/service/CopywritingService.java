package com.xinpa.service;

import com.xinpa.entity.Copywriting;

import java.util.List;

/**
 * 快捷文案服务接口
 */
public interface CopywritingService {

    /**
     * 查询文案列表
     */
    List<Copywriting> listByUserId(Long userId, String category);

    /**
     * 创建文案
     */
    void create(Copywriting copywriting);

    /**
     * 更新文案
     */
    void update(Copywriting copywriting);

    /**
     * 删除文案
     */
    void delete(Long id, Long userId);
}
