package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "lich_su_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuThanhToan extends PrimaryEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    // ma_giao_dich VARCHAR(100),
    // so_tien DECIMAL(18,2),
    // ngay_thanh_toan DATETIME,
    // hinh_thuc_thanh_toan NVARCHAR(100),
    // loai_thanh_toan NVARCHAR(100),
    // ghi_chu NVARCHAR(MAX),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;
    @Column(name = "ma_giao_dich")
    private String maGiaoDich;
    @Column(name = "so_tien")
    private BigDecimal soTien;
    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;
    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;
    @Column(name = "loai_thanh_toan")
    private String loaiThanhToan;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @Column(name = "trang_thai")
    private Integer trangThai;
}