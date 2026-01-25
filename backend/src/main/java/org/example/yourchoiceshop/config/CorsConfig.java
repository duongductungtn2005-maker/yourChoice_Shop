package org.example.yourchoiceshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho mọi đường dẫn
                .allowedOrigins("http://localhost:5173") // Chỉ cho phép VueJS truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ⚠️ Quan trọng: Phải có DELETE
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}