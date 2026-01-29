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
public class PriceService {

    private final ChiTietSanPhamRepository productRepo;
    private final DotGiamGiaRepository promotionRepo;

    /**
     * Hàm này nhận vào ID sản phẩm và trả về giá bán tốt nhất hiện tại.
     * Có thể dùng hàm này ở bất cứ đâu (khi bán hàng tại quầy, khi tạo hóa đơn, v.v.)
     */
    public BigDecimal calculateBestPrice(Integer productDetailId) {
        // 1. Lấy giá gốc
        ChiTietSanPham product = productRepo.findById(productDetailId).orElse(null);
        if (product == null) return BigDecimal.ZERO;

        BigDecimal originalPrice = product.getGiaBan();
        BigDecimal bestPrice = originalPrice; // Mặc định là giá gốc nếu không có KM

        // 2. Tìm tất cả khuyến mãi đang chạy cho SP này
        List<DotGiamGia> activePromotions = promotionRepo.findValidPromotionsForProduct(productDetailId, LocalDateTime.now());

        // 3. So sánh để tìm giá thấp nhất (Lợi nhất cho khách)
        for (DotGiamGia promo : activePromotions) {
            BigDecimal tempPrice = originalPrice;

            if ("VND".equalsIgnoreCase(promo.getLoaiGiamGia())) {
                // Giảm tiền mặt
                tempPrice = originalPrice.subtract(promo.getGiaTriGiam());
            } else {
                // Giảm phần trăm
                BigDecimal percent = promo.getGiaTriGiam().divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = originalPrice.multiply(percent);
                tempPrice = originalPrice.subtract(discountAmount);
            }

            // Nếu giá sau giảm này rẻ hơn giá tốt nhất hiện tại -> Cập nhật
            if (tempPrice.compareTo(bestPrice) < 0) {
                bestPrice = tempPrice;
            }
        }

        // Không trả về số âm
        return bestPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : bestPrice;
    }
}