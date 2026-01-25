package org.example.yourchoiceshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class YourChoiceShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourChoiceShopApplication.class, args);
    }

}
