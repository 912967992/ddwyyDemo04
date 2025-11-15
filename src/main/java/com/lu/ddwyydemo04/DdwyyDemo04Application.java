package com.lu.ddwyydemo04;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@MapperScan("com.lu.ddwyydemo04.dao")
@SpringBootApplication
@EnableTransactionManagement // 启用事务管理
@EnableScheduling
public class DdwyyDemo04Application {

    public static void main(String[] args) {
        // 设置HTTP连接超时时间（单位：毫秒）
        // 连接超时：30秒（避免用户等待过久）
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        // 读取超时：30秒
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");
        
        SpringApplication.run(DdwyyDemo04Application.class, args);
    }

}
//