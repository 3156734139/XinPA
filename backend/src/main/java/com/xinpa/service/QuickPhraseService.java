package com.xinpa.service;

import com.xinpa.entity.QuickPhrase;

import java.util.List;

/**
 * 快捷短语服务接口
 */
public interface QuickPhraseService {

    /**
     * 获取用户短语列表
     */
    List<QuickPhrase> listByUserId(Long userId);

    /**
     * 添加短语
     */
    void create(QuickPhrase phrase);

    /**
     * 删除短语
     */
    void delete(Long id, Long userId);
}
