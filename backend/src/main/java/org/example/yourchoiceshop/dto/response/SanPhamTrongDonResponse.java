package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SanPhamTrongDonResponse {
    private String tenSanPham;
    private String size;
    private String mauSac;
    private Integer soLuong;
    private BigDecimal thanhTien;
}
