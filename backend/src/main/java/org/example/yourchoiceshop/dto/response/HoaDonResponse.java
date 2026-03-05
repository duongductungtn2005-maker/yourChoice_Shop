package org.example.yourchoiceshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private Integer tongSanPham;
    private BigDecimal tongTienSauGiam;
    private String tenKhachHang;
    private String maNhanVien;
    private String tenNhanVien;
    private LocalDateTime ngayTao;
    private String loaiHoaDon;
    private Integer trangThai;
    private String emailKhachHang;
}