package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity 
@Table(name = "phieu_giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGia extends PrimaryEntity { // SQL không có audit fields đầy đủ
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_phieu_giam_gia VARCHAR(50) UNIQUE,
    // ten_phieu_giam_gia NVARCHAR(255),
    // loai_phieu NVARCHAR(50),
    // gia_tri_giam DECIMAL(18,2),
    // gia_tri_giam_toi_da DECIMAL(18,2),
    // don_hang_toi_thieu DECIMAL(18,2),
    // so_luong INT,
    // ngay_bat_dau DATETIME,
    // ngay_ket_thuc DATETIME,
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_phieu_giam_gia", unique = true)
    private String maPhieuGiamGia;
    @Column(name = "ten_phieu_giam_gia")
    private String tenPhieuGiamGia;
    @Column(name = "loai_phieu")
    private String loaiPhieu;
    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;
    @Column(name = "gia_tri_giam_toi_da")
    private BigDecimal giaTriGiamToiDa;
    @Column(name = "don_hang_toi_thieu")
    private BigDecimal donHangToiThieu;
    @Column(name = "so_luong")
    private Integer soLuong;
    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;
    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;
    @Column(name = "trang_thai")
    private Integer trangThai;
}