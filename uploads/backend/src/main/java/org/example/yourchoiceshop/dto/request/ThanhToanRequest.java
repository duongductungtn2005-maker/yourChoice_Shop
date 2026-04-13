package org.example.yourchoiceshop.dto.request;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
public class ThanhToanRequest {

    // 🔥 THÊM
    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTienSauGiam;

    // giữ nguyên
    private BigDecimal soTien;
    private String hinhThucThanhToan;
    private String ghiChu;
}