package org.example.yourchoiceshop.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class EmployeeRequest {
    private String tenNhanVien;
    private String email;
    private String soDienThoai;
    private String cccd;
    private Boolean gioiTinh;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;

    private String anhDaiDien;

    // 3 trường địa chỉ từ Frontend
    private String city;
    private String district;
    private String ward;
    private String address; // Số nhà cụ thể

    private MultipartFile avatarFile;
    private String diaChi;
    // --- [BỔ SUNG QUAN TRỌNG] ---
    private String chucVu; // Nhận giá trị "ADMIN" hoặc "STAFF"
}