package com.xinpa.service;

import com.xinpa.entity.FollowUpReminder;

import java.util.List;

/**
 * 回访提醒服务接口
 */
public interface FollowUpReminderService {

    /**
     * 获取用户回访提醒列表
     */
    List<FollowUpReminder> listByUserId(Long userId, Integer status);

    /**
     * 创建回访提醒
     */
    void create(FollowUpReminder reminder);

    /**
     * 处理回访
     */
    void handle(Long id, Long userId);

    /**
     * 忽略回访
     */
    void ignore(Long id, Long userId);

    /**
     * 自动生成回访提醒（根据订单完成）
     */
    void autoGenerate(Long userId, Long customerId, Long orderId);
}
