package org.example.yourchoiceshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Dòng dưới này mới là đường dẫn đúng của Tomcat Factory
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory; 
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YourChoiceShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourChoiceShopApplication.class, args);
    }

    // CẤU HÌNH FIX LỖI UPLOAD 500 TẠI ĐÂY
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            // Đặt -1 để không giới hạn số lượng tham số
            connector.setProperty("maxParameterCount", "-1");
            // Đặt -1 để không giới hạn dung lượng post
            connector.setMaxPostSize(-1);
        });
    }
}