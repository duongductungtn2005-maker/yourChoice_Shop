package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
// import java.sql.Date;
import java.time.LocalDate; // Nhớ import dòng này
@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang extends BaseStatusEntity {

    // Đã xóa: id (do PrimaryEntity quản lý)
    // Đã xóa: ngayTao, trangThai... (do BaseStatusEntity quản lý)
    // Chỉ giữ lại thông tin riêng của Khách hàng:

    @Column(name = "ma_khach_hang", unique = true)
    private String maKhachHang;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "ten_tai_khoan")
    private String tenTaiKhoan;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "email")
    private String email;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;
    @Column(name = "trang_thai")
    private Integer trangThai;

    // --- BỔ SUNG TRƯỜNG NÀY ---
    @Column(name = "avatar") // Hoặc "anh_dai_dien" tùy DB của bạn
    private String avatar;
}