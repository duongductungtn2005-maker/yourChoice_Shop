package org.example.yourchoiceshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. Cấu hình mở quyền truy cập ảnh (Quan trọng nhất)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Khi gọi http://localhost:8080/images/ten-anh.jpg
        // -> Nó sẽ tìm file trong thư mục "uploads/" (relative to working directory)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/");
    }

    // 2. Cấu hình CORS (Để tránh lỗi chặn truy cập từ Frontend)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả API
                .allowedOrigins("*") // Cho phép tất cả các trang web truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); // Các method cho phép
    }
}