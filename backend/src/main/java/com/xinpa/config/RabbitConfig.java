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

    /** 回访提醒交换机 */
    public static final String FOLLOW_UP_EXCHANGE = "xinpa.followup";
    /** 回访提醒队列 */
    public static final String FOLLOW_UP_QUEUE = "xinpa.followup.queue";
    /** 回访提醒路由键 */
    public static final String FOLLOW_UP_ROUTING_KEY = "followup.remind";

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

    // ==================== 回访提醒 ====================

    @Bean
    public DirectExchange followUpExchange() {
        return new DirectExchange(FOLLOW_UP_EXCHANGE);
    }

    @Bean
    public Queue followUpQueue() {
        return QueueBuilder.durable(FOLLOW_UP_QUEUE).build();
    }

    @Bean
    public Binding followUpBinding() {
        return BindingBuilder.bind(followUpQueue())
                .to(followUpExchange())
                .with(FOLLOW_UP_ROUTING_KEY);
    }
}
