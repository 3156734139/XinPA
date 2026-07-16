package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.FollowUpReminder;
import com.xinpa.mapper.FollowUpReminderMapper;
import com.xinpa.service.FollowUpReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回访提醒服务实现
 */
@Service
@RequiredArgsConstructor
public class FollowUpReminderServiceImpl implements FollowUpReminderService {

    private final FollowUpReminderMapper followUpReminderMapper;

    @Override
    public List<FollowUpReminder> listByUserId(Long userId, Integer status) {
        LambdaQueryWrapper<FollowUpReminder> wrapper = new LambdaQueryWrapper<FollowUpReminder>()
                .eq(FollowUpReminder::getUserId, userId)
                .eq(status != null, FollowUpReminder::getStatus, status)
                .orderByAsc(FollowUpReminder::getRemindTime);
        return followUpReminderMapper.selectList(wrapper);
    }

    @Override
    public void create(FollowUpReminder reminder) {
        followUpReminderMapper.insert(reminder);
    }

    @Override
    public void handle(Long id, Long userId) {
        FollowUpReminder reminder = followUpReminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在");
        }
        reminder.setStatus(1);
        followUpReminderMapper.updateById(reminder);
    }

    @Override
    public void ignore(Long id, Long userId) {
        FollowUpReminder reminder = followUpReminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在");
        }
        reminder.setStatus(2);
        followUpReminderMapper.updateById(reminder);
    }

    @Override
    public void autoGenerate(Long userId, Long customerId, Long orderId) {
        // 下单3天后回访
        FollowUpReminder r1 = new FollowUpReminder();
        r1.setUserId(userId);
        r1.setCustomerId(customerId);
        r1.setOrderId(orderId);
        r1.setRemindType(1);
        r1.setRemindTime(LocalDateTime.now().plusDays(3));
        r1.setStatus(0);
        followUpReminderMapper.insert(r1);

        // 下单7天后回访
        FollowUpReminder r2 = new FollowUpReminder();
        r2.setUserId(userId);
        r2.setCustomerId(customerId);
        r2.setOrderId(orderId);
        r2.setRemindType(2);
        r2.setRemindTime(LocalDateTime.now().plusDays(7));
        r2.setStatus(0);
        followUpReminderMapper.insert(r2);
    }
}
