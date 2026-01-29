package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.DotGiamGia;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.DotGiamGiaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientProductService {

    private final ChiTietSanPhamRepository productRepo;
    private final DotGiamGiaRepository promotionRepo;

    // Hàm tính giá tốt nhất cho 1 sản phẩm cụ thể
    public BigDecimal calculateBestPrice(Integer productDetailId) {
        ChiTietSanPham product = productRepo.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("SP không tồn tại"));

        BigDecimal originalPrice = product.getGiaBan();
        BigDecimal bestPrice = originalPrice; // Mặc định là giá gốc

        // 1. Lấy danh sách các đợt giảm giá hợp lệ hiện tại
        List<DotGiamGia> activePromotions = promotionRepo.findValidPromotionsForProduct(productDetailId, LocalDateTime.now());

        // 2. Loop để tìm giá thấp nhất
        for (DotGiamGia promo : activePromotions) {
            BigDecimal currentDiscountedPrice = originalPrice;

            if ("VND".equalsIgnoreCase(promo.getLoaiGiamGia())) {
                // Giảm tiền mặt
                currentDiscountedPrice = originalPrice.subtract(promo.getGiaTriGiam());
            } else {
                // Giảm %
                BigDecimal percent = promo.getGiaTriGiam().divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = originalPrice.multiply(percent);
                currentDiscountedPrice = originalPrice.subtract(discountAmount);
            }

            // So sánh: Nếu giá này rẻ hơn giá tốt nhất hiện tại -> Cập nhật
            if (currentDiscountedPrice.compareTo(bestPrice) < 0) {
                bestPrice = currentDiscountedPrice;
            }
        }

        // Không để giá âm
        return bestPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : bestPrice;
    }
}