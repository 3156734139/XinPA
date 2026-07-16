package com.xinpa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 陪玩星助手 - 启动类
 */
@SpringBootApplication
@MapperScan("com.xinpa.mapper")
@EnableScheduling
public class XinpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(XinpaApplication.class, args);
    }
}
