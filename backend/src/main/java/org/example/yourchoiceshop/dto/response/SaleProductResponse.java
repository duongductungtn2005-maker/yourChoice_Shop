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
public class SaleProductResponse {
    private Integer id;             // ID bảng chi_tiet_dot_giam_gia
    private String maSanPham;       // Mã CTSP
    private String tenSanPham;      // Tên sản phẩm
    private String mauSac;          // Tên màu
    private String kichThuoc;       // Tên size
    private BigDecimal giaGoc;      // Giá bán gốc
    private BigDecimal giaSauGiam;  // Giá sau khi trừ khuyến mãi
    private Integer soLuongApDung;  // Số lượng phân bổ cho đợt này
}