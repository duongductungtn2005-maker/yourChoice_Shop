package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Integer id;             // ID của ChiTietSanPham
    private String maSanPham;       // Mã SP
    private String tenSanPham;      // Tên SP
    private String mauSac;          // Màu
    private String kichThuoc;       // Size
    private BigDecimal giaBan;      // Giá bán
    private Integer soLuong;        // Số lượng tồn
}