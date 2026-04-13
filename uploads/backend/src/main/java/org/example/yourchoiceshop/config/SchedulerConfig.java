package org.example.yourchoiceshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Chỉ cần khai báo thế này để báo cho Spring biết là project có dùng tính năng hẹn giờ
}
