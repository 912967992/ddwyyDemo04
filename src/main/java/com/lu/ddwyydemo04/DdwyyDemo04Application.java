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
        SpringApplication.run(DdwyyDemo04Application.class, args);
    }

}
//