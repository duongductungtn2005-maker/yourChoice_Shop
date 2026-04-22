package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;


@Data
public class QuanLyDonHangResponse {
    private Integer id;
    private String maHoaDon;

    // COUNT(hdct)
    private Integer tongSanPham;

    private BigDecimal tongTienSauGiam;

    private String tenKhachHang;

    private LocalDateTime ngayTao;

    private String loaiHoaDon;

    private Integer trangThai;
}
