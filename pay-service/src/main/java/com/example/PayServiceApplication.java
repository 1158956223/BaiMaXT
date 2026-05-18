package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@MapperScan("com.example.mapper")
@SpringBootApplication
public class PayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayServiceApplication.class, args);
    }
}
