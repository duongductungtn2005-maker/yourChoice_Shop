package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSanPham extends BaseStatusEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_ctsp VARCHAR(50) UNIQUE,
    // so_luong INT,
    // gia_nhap DECIMAL(18,2),
    // gia_ban DECIMAL(18,2),
    // id_san_pham INT FOREIGN KEY REFERENCES san_pham(id),
    // id_mau_sac INT FOREIGN KEY REFERENCES mau_sac(id),
    // id_kich_thuoc INT FOREIGN KEY REFERENCES kich_thuoc(id),
    // ngay_tao DATETIME,
    // ngay_cap_nhat DATETIME,
    // nguoi_tao NVARCHAR(100),
    // nguoi_cap_nhat NVARCHAR(100),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_ctsp", unique = true)
    private String maCtsp;
    @Column(name = "so_luong")
    private Integer soLuong;
    @Column(name = "gia_nhap")
    private BigDecimal giaNhap;
    @Column(name = "gia_ban")
    private BigDecimal giaBan;
    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;
    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;
    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;
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