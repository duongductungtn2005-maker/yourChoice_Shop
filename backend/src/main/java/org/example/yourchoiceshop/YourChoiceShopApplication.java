package org.example.yourchoiceshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <--- NHỚ PHẢI CÓ DÒNG NÀY THÌ 17H NÓ MỚI TỰ CHẠY ĐƯỢC
@EnableAsync
public class YourChoiceShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourChoiceShopApplication.class, args);
    }

}