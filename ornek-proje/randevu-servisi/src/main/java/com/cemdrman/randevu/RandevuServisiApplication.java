package com.cemdrman.randevu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RandevuServisiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RandevuServisiApplication.class, args);
    }
}
