package org.example.yourchoiceshop.config;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.repository.DotGiamGiaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PromotionScheduler {

    private final DotGiamGiaRepository repo;

    // Chạy mỗi 1 phút (60000ms)
    @Scheduled(fixedRate = 60000)
    public void autoUpdatePromotionStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Bật đợt giảm giá
        repo.updateStatusToActive(now);

        // 2. Tắt đợt giảm giá
        repo.updateStatusToExpired(now);

        System.out.println("Đã cập nhật trạng thái khuyến mãi lúc: " + now);
    }
}