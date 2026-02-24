package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;

import lombok.*;


@AllArgsConstructor // <--- QUAN TRỌNG: Để Hibernate map được dữ liệu từ câu SELECT new...
@Data
@Getter
@Setter
public class SanPhamHoaDonResponse {
    private Integer idHoaDonChiTiet;

    private String tenSanPham;
    private String mauSac;
    private String kichThuoc;

    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    private String duongDanAnh;
}
