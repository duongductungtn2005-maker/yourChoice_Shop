package org.example.yourchoiceshop.dto.response;

import java.util.Date;

public interface KhachHangThongKeResponse {
    Integer getId();
    String getTenKhachHang();
    String getSoDienThoai();
    String getEmail();
    Date getNgaySinh();
    Integer getTrangThai();
    
    // 3 trường thống kê
    Number getTongChiTieu();
    Number getSoDonHang();
    Date getDonHangGanNhat();
}