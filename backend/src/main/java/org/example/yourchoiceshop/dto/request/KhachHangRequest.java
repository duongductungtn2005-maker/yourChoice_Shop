package org.example.yourchoiceshop.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class KhachHangRequest {
    private String tenKhachHang;
    private String email;
    private String soDienThoai;
    private String cccd; // DB bạn chưa có cột CCCD, tạm thời code sẽ bỏ qua hoặc lưu vào ghi chú
    private Boolean gioiTinh;
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Thêm dòng này
    private LocalDate ngaySinh;
    private String anhDaiDien;
    // 3 trường địa chỉ từ Frontend -> gộp thành 1 chuỗi lưu vào DB
    private String city;
    private String district;
    private String ward;
    private String address; // Số nhà cụ thể

    private MultipartFile avatarFile; // File ảnh upload
}