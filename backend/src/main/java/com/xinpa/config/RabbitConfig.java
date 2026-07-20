package com.xinpa.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置
 */
@Configuration
public class RabbitConfig {

    /** 预约提醒交换机 */
    public static final String APPOINTMENT_EXCHANGE = "xinpa.appointment";
    /** 预约提醒队列 */
    public static final String APPOINTMENT_QUEUE = "xinpa.appointment.queue";
    /** 预约提醒路由键 */
    public static final String APPOINTMENT_ROUTING_KEY = "appointment.remind";

    // ==================== 预约提醒 ====================

    @Bean
    public DirectExchange appointmentExchange() {
        return new DirectExchange(APPOINTMENT_EXCHANGE);
    }

    @Bean
    public Queue appointmentQueue() {
        return QueueBuilder.durable(APPOINTMENT_QUEUE).build();
    }

    @Bean
    public Binding appointmentBinding() {
        return BindingBuilder.bind(appointmentQueue())
                .to(appointmentExchange())
                .with(APPOINTMENT_ROUTING_KEY);
    }
}
