package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.QuickPhrase;
import com.xinpa.mapper.QuickPhraseMapper;
import com.xinpa.service.QuickPhraseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 快捷短语服务实现
 */
@Service
@RequiredArgsConstructor
public class QuickPhraseServiceImpl implements QuickPhraseService {

    private final QuickPhraseMapper quickPhraseMapper;

    @Override
    public List<QuickPhrase> listByUserId(Long userId) {
        return quickPhraseMapper.selectList(
                new LambdaQueryWrapper<QuickPhrase>()
                        .eq(QuickPhrase::getUserId, userId)
                        .eq(QuickPhrase::getDeleted, 0)
                        .orderByAsc(QuickPhrase::getSortOrder));
    }

    @Override
    public void create(QuickPhrase phrase) {
        quickPhraseMapper.insert(phrase);
    }

    @Override
    public void delete(Long id, Long userId) {
        QuickPhrase phrase = quickPhraseMapper.selectById(id);
        if (phrase == null || !phrase.getUserId().equals(userId)) {
            throw new BusinessException("短语不存在");
        }
        quickPhraseMapper.deleteById(id);
    }
}
