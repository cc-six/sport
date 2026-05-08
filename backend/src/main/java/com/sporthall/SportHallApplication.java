package com.sporthall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sporthall.mapper")
public class SportHallApplication {
    public static void main(String[] args) {
        SpringApplication.run(SportHallApplication.class, args);
    }
}
