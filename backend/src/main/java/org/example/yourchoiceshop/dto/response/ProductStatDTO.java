package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatDTO {
    private String maSanPham;
    private String tenSanPham;
    private Long soLuongBan;     // Tổng số lượng đã bán
    private BigDecimal doanhThu; // Tổng tiền thu được từ sản phẩm này
    private String kichCo;       // THÊM MỚI
    private String anh;
}