package com.materialmaker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.materialmaker.mapper")
public class MaterialMakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaterialMakerApplication.class, args);
    }
}
