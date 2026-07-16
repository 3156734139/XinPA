package com.xinpa.mq;

import com.xinpa.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 回访提醒消费者
 */
@Slf4j
@Component
public class FollowUpConsumer {

    @RabbitListener(queues = RabbitConfig.FOLLOW_UP_QUEUE)
    public void handleFollowUpRemind(Map<String, Object> data) {
        try {
            Long userId = (Long) data.get("userId");
            Long customerId = (Long) data.get("customerId");
            String customerName = (String) data.get("customerName");

            log.info("回访提醒: 用户[{}] 客户[{}] - {}", userId, customerId, customerName);
        } catch (Exception e) {
            log.error("回访提醒处理失败", e);
        }
    }
}
