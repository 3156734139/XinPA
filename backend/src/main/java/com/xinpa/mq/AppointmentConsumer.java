package com.xinpa.mq;

import com.rabbitmq.client.Channel;
import com.xinpa.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 预约提醒消费者
 */
@Slf4j
@Component
public class AppointmentConsumer {

    @RabbitListener(queues = RabbitConfig.APPOINTMENT_QUEUE)
    public void handleAppointmentRemind(Map<String, Object> data, Channel channel, Message message) {
        try {
            Long userId = (Long) data.get("userId");
            Long appointmentId = (Long) data.get("appointmentId");
            String title = (String) data.get("title");

            log.info("预约提醒: 用户[{}] 预约[{}] - {}", userId, appointmentId, title);
            // 实际推送通过WebSocket或轮询实现
        } catch (Exception e) {
            log.error("预约提醒处理失败", e);
        }
    }
}
