package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dia_chi_khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiKhachHang extends PrimaryEntity {

    @Column(name = "ma_dia_chi")
    private String maDiaChi;

    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "thanh_pho")
    private String thanhPho;

    @Column(name = "quan")
    private String quan;

    @Column(name = "phuong")
    private String phuong;

    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @Column(name = "mac_dinh")
    private Boolean macDinh;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    @JsonBackReference // Ngăn không cho in lại KhachHang bên trong DiaChi (tránh lặp)
    private KhachHang khachHang;
}