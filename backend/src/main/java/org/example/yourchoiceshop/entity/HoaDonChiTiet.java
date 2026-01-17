package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity
@Table(name = "hoa_don_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTiet extends PrimaryEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_hoa_don_chi_tiet VARCHAR(50),
    // so_luong INT,
    // don_gia DECIMAL(18,2),
    // thanh_tien DECIMAL(18,2),
    // ghi_chu NVARCHAR(MAX),
    // id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    // id_chi_tiet_san_pham INT FOREIGN KEY REFERENCES chi_tiet_san_pham(id),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_hoa_don_chi_tiet")
    private String maHoaDonChiTiet;
    @Column(name = "so_luong")
    private Integer soLuong;
    @Column(name = "don_gia")
    private BigDecimal donGia;
    @Column(name = "thanh_tien")
    private BigDecimal thanhTien;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @ManyToOne @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;
    @ManyToOne @JoinColumn(name = "id_chi_tiet_san_pham") private ChiTietSanPham chiTietSanPham;
}