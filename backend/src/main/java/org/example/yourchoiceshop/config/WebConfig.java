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
        // -> Nó sẽ tìm file trong thư mục "uploads" nằm ngang hàng với file pom.xml
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./uploads/");
    }

    // 2. Cấu hình CORS (Để tránh lỗi chặn truy cập từ Frontend)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // Port của VueJS
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}