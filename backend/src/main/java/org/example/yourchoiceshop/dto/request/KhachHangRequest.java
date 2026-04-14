package org.example.yourchoiceshop.dto.request;

import java.time.LocalDate;
import java.util.List; // Import List

import org.example.yourchoiceshop.entity.DiaChiKhachHang; // Import Entity Địa chỉ
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class KhachHangRequest {
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;

    // Bổ sung username/password để khớp với Entity
    private String username;
    private String password;

    private Boolean gioiTinh;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;

    private String anhDaiDien;

    // Các trường lẻ này có thể giữ hoặc bỏ nếu đã dùng List bên dưới
    private String city;
    private String district;
    private String ward;
    private String address;

    private Integer trangThai;
    private MultipartFile avatarFile;

    // --- BỔ SUNG QUAN TRỌNG ---
    private List<DiaChiKhachHang> listDiaChi;
}