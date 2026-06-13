package com.example.futurecommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.futurecommunity.mapper")
@EnableAsync
public class FutureCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(FutureCommunityApplication.class, args);
    }

}
