package org.example.yourchoiceshop.vnpay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VnPayConfig.VnPayProperties.class)
public class VnPayConfig {

    @Data
    @ConfigurationProperties(prefix = "vnpay")
    public static class VnPayProperties {
        private String tmnCode;
        private String hashSecret;
        private String payUrl;
        private String returnUrl;
        private String apiUrl;
    }
}
