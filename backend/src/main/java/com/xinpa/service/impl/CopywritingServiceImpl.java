package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Copywriting;
import com.xinpa.mapper.CopywritingMapper;
import com.xinpa.service.CopywritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 快捷文案服务实现
 */
@Service
@RequiredArgsConstructor
public class CopywritingServiceImpl implements CopywritingService {

    private final CopywritingMapper copywritingMapper;

    @Override
    public List<Copywriting> listByUserId(Long userId, String category) {
        LambdaQueryWrapper<Copywriting> wrapper = new LambdaQueryWrapper<Copywriting>()
                .eq(Copywriting::getUserId, userId)
                .eq(Copywriting::getDeleted, 0)
                .eq(category != null, Copywriting::getCategory, category)
                .orderByDesc(Copywriting::getCreatedAt);
        return copywritingMapper.selectList(wrapper);
    }

    @Override
    public void create(Copywriting copywriting) {
        copywritingMapper.insert(copywriting);
    }

    @Override
    public void update(Copywriting copywriting) {
        copywritingMapper.updateById(copywriting);
    }

    @Override
    public void delete(Long id, Long userId) {
        Copywriting copywriting = copywritingMapper.selectById(id);
        if (copywriting == null || !copywriting.getUserId().equals(userId)) {
            throw new BusinessException("文案不存在");
        }
        copywritingMapper.deleteById(id);
    }
}
