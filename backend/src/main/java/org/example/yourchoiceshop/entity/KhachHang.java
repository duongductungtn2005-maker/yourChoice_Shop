package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "khach_hang") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang extends BaseStatusEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_khach_hang VARCHAR(50) UNIQUE,
    // ten_khach_hang NVARCHAR(255),
    // ten_tai_khoan VARCHAR(100),
    // mat_khau VARCHAR(255),
    // email VARCHAR(255),
    // so_dien_thoai VARCHAR(15),
    // gioi_tinh BIT,
    // ngay_sinh DATE,
    // ngay_tao DATETIME,
    // ngay_cap_nhat DATETIME,
    // nguoi_tao NVARCHAR(100),
    // nguoi_cap_nhat NVARCHAR(100),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
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
    private Date ngaySinh;
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    @Column(name = "nguoi_tao")
    private String nguoiTao;
    @Column(name = "nguoi_cap_nhat")
    private String nguoiCapNhat;
    @Column(name = "trang_thai")
    private Integer trangThai;
}