package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Appointment;
import com.xinpa.mapper.AppointmentMapper;
import com.xinpa.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约日历服务实现
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;

    @Override
    public List<Appointment> listByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getUserId, userId)
                        .eq(Appointment::getDeleted, 0)
                        .ge(start != null, Appointment::getStartTime, start)
                        .le(end != null, Appointment::getEndTime, end)
                        .orderByAsc(Appointment::getStartTime));
    }

    @Override
    public void create(Appointment appointment) {
        // 检查冲突
        List<Appointment> conflicts = checkConflict(
                appointment.getUserId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                null);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段已有预约，请调整时间");
        }
        appointmentMapper.insert(appointment);
    }

    @Override
    public void update(Appointment appointment) {
        List<Appointment> conflicts = checkConflict(
                appointment.getUserId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getId());
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段已有预约，请调整时间");
        }
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void delete(Long id, Long userId) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new BusinessException("预约不存在");
        }
        appointmentMapper.deleteById(id);
    }

    @Override
    public List<Appointment> checkConflict(Long userId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .eq(Appointment::getDeleted, 0)
                .lt(Appointment::getStartTime, end)
                .gt(Appointment::getEndTime, start);
        if (excludeId != null) {
            wrapper.ne(Appointment::getId, excludeId);
        }
        return appointmentMapper.selectList(wrapper);
    }
}
