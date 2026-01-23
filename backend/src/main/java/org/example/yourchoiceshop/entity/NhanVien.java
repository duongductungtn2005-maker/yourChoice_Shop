package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien extends BaseStatusEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_nhan_vien VARCHAR(50) UNIQUE,
    // ten_nhan_vien NVARCHAR(255),
    // mat_khau VARCHAR(255),
    // email VARCHAR(255),
    // so_dien_thoai VARCHAR(15),
    // gioi_tinh BIT,
    // ngay_sinh DATE,
    // dia_chi NVARCHAR(MAX),
    // id_quyen_han INT FOREIGN KEY REFERENCES quyen_han(id),
    // ngay_tao DATETIME,
    // ngay_cap_nhat DATETIME,
    // nguoi_tao NVARCHAR(100),
    // nguoi_cap_nhat NVARCHAR(100),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_nhan_vien", unique = true)
    private String maNhanVien;
    @Column(name = "ten_nhan_vien")
    private String tenNhanVien;
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
    @Column(name = "dia_chi")
    private String diaChi;
    @ManyToOne
    @JoinColumn(name = "id_quyen_han")
    private QuyenHan quyenHan;
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