package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity 
@Table(name = "hoa_don") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon extends BaseStatusEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_hoa_don VARCHAR(50) UNIQUE,
    // ngay_thanh_toan DATETIME,
    // tong_tien DECIMAL(18,2),
    // tien_giam_gia DECIMAL(18,2),
    // phi_van_chuyen DECIMAL(18,2),
    // tong_tien_sau_giam DECIMAL(18,2),
    // hinh_thuc_thanh_toan NVARCHAR(100),
    // loai_hoa_don NVARCHAR(50),
    // ten_nguoi_nhan NVARCHAR(255),
    // sdt_nguoi_nhan VARCHAR(15),
    // email_khach_hang VARCHAR(255),
    // dia_chi_nguoi_nhan NVARCHAR(MAX),
    // ghi_chu NVARCHAR(MAX),
    // id_khach_hang INT FOREIGN KEY REFERENCES khach_hang(id),
    // id_nhan_vien INT FOREIGN KEY REFERENCES nhan_vien(id),
    // id_phieu_giam_gia INT FOREIGN KEY REFERENCES phieu_giam_gia(id),
    // ngay_tao DATETIME,
    // ngay_cap_nhat DATETIME,
    // nguoi_tao NVARCHAR(100),
    // nguoi_cap_nhat NVARCHAR(100),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_hoa_don", unique = true)
    private String maHoaDon;
    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;
    @Column(name = "tong_tien")
    private BigDecimal tongTien;
    @Column(name = "tien_giam_gia")
    private BigDecimal tienGiamGia;
    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;
    @Column(name = "tong_tien_sau_giam")
    private BigDecimal tongTienSauGiam;
    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;
    @Column(name = "loai_hoa_don")
    private String loaiHoaDon;
    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;
    @Column(name = "sdt_nguoi_nhan")
    private String sdtNguoiNhan;
    @Column(name = "email_khach_hang")
    private String emailKhachHang;
    @Column(name = "dia_chi_nguoi_nhan", columnDefinition = "NVARCHAR(MAX)")
    private String diaChiNguoiNhan;
    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;
    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia phieuGiamGia;
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
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> chiTietHoaDons;
}