package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Import để xử lý vòng lặp JSON
import jakarta.persistence.*;
import lombok.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List; // Import List

@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang extends BaseStatusEntity {

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

    @Column(name = "avatar")
    private String avatar;

    // --- BỔ SUNG QUAN TRỌNG ĐỂ LẤY ĐỊA CHỈ ---
    // 1. mappedBy = "khachHang": Tên biến trong Entity DiaChi trỏ về KhachHang
    // 2. fetch = FetchType.EAGER: Bắt buộc tải luôn địa chỉ khi lấy khách hàng (Fix lỗi không hiện data)
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference // Giúp hiển thị list địa chỉ trong JSON mà không bị lỗi lặp vô tận
    private List<DiaChiKhachHang> listDiaChi;
}