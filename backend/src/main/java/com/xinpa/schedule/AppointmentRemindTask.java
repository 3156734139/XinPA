package com.xinpa.schedule;

import com.xinpa.entity.Appointment;
import com.xinpa.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约提醒定时任务（每5分钟检查一次）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentRemindTask {

    private final AppointmentMapper appointmentMapper;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 300000)
    public void checkAppointmentRemind() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remindTime = now.plusMinutes(10);

        List<Appointment> appointments = appointmentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDeleted, 0)
                        .between(Appointment::getStartTime, now, remindTime));

        for (Appointment appointment : appointments) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", appointment.getUserId());
            data.put("appointmentId", appointment.getId());
            data.put("title", appointment.getTitle());

            rabbitTemplate.convertAndSend(
                    com.xinpa.config.RabbitConfig.APPOINTMENT_EXCHANGE,
                    com.xinpa.config.RabbitConfig.APPOINTMENT_ROUTING_KEY,
                    data);

            log.info("已发送预约提醒消息: 预约ID={}", appointment.getId());
        }
    }
}
