package org.example.yourchoiceshop.config;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.service.impl.DotGiamGiaServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscountScheduler {

    private final ChiTietSanPhamRepository ctspRepo;
    private final DotGiamGiaServiceImpl dotGiamGiaService;

    // Chạy tự động mỗi 1 phút (Để test cho nhanh, thực tế có thể set 1 tiếng)
    @Scheduled(cron = "0 * * * * ?") 
    public void scanAndApplyBestDiscounts() {
        System.out.println("--- Đang quét và cập nhật lại đợt giảm giá tốt nhất ---");
        
        // Tối ưu: Chỉ lấy các sản phẩm đang có đợt giảm giá hoặc vừa hết hạn 
        // (Ở đây tao lấy tạm findAll cho m dễ hiểu, nễu dữ liệu lớn thì phải query tối ưu)
        List<ChiTietSanPham> allProducts = ctspRepo.findAll();
        
        for (ChiTietSanPham ctsp : allProducts) {
            dotGiamGiaService.updateBestDiscountForProduct(ctsp);
        }
        
        ctspRepo.saveAll(allProducts);
    }
}