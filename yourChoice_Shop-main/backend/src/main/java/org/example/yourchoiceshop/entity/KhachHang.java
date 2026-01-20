package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity @Table(name = "khach_hang") @Data
public class KhachHang extends BaseStatusEntity {
    @Column(name = "ma_khach_hang") private String maKhachHang;
    @Column(name = "ten_khach_hang") private String tenKhachHang;
    @Column(name = "ten_tai_khoan") private String tenTaiKhoan;
    private String matKhau;
    private String email;
    @Column(name = "so_dien_thoai") private String soDienThoai;
    private Boolean gioiTinh;
    @Column(name = "ngay_sinh") private LocalDate ngaySinh;

    @OneToMany(mappedBy = "khachHang") private List<DiaChiKhachHang> diaChiKhachHangs;
}