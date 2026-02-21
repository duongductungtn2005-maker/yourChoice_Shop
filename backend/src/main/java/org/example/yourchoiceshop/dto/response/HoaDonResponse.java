package org.example.yourchoiceshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HoaDonResponse {
    // Dành cho màn danh sách (QuanLyDonHang.vue)
    private String maHoaDon;        // Map vào 'code'
    private Integer tongSanPham;    // Map vào 'totalItems'
    private BigDecimal tongTienSauGiam; // Map vào 'totalPrice'
    private String tenKhachHang;    // Map vào 'customer'
    private LocalDateTime ngayTao;  // Map vào 'createdAt'
    private String loaiHoaDon;      // Map vào 'type'
    private Integer trangThai;      // Map vào 'status'
}