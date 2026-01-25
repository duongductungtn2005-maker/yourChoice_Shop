package org.example.yourchoiceshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // <--- Annotation này kích hoạt tính năng tự sinh ngày
public class JpaAuditingConfig {
}
