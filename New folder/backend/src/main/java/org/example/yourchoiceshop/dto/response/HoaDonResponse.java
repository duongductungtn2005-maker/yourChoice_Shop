package org.example.yourchoiceshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HoaDonResponse {
    private Integer id; // <--- THÊM DÒNG NÀY (Để sửa lỗi cannot find symbol setId)

    private String maHoaDon;
    private Integer tongSanPham;
    private BigDecimal tongTienSauGiam;
    private String tenKhachHang;
    private LocalDateTime ngayTao;
    private String loaiHoaDon;
    private Integer trangThai;
}