package com.xinpa.service;

import com.xinpa.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约日历服务接口
 */
public interface AppointmentService {

    /**
     * 获取用户预约列表（按时间范围）
     */
    List<Appointment> listByDateRange(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * 创建预约
     */
    void create(Appointment appointment);

    /**
     * 更新预约
     */
    void update(Appointment appointment);

    /**
     * 删除预约
     */
    void delete(Long id, Long userId);

    /**
     * 检查时间冲突
     */
    List<Appointment> checkConflict(Long userId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
