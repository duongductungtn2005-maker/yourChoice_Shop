package org.example.yourchoiceshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync // Bắt buộc phải có để gửi mail không bị treo ứng dụng
@EnableScheduling
public class YourChoiceShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourChoiceShopApplication.class, args);
    }

}