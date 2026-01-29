package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;

import lombok.*;



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

    private String hinhAnh;
}
