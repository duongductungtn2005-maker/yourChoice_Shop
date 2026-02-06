package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.yourchoiceshop.entity.enums.HinhThucThanhToan;
import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;

@Entity
@Builder
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon extends BaseStatusEntity {

    @Column(name = "ma_hoa_don", unique = true)
    private String maHoaDon;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @NotNull
    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "tien_giam_gia")
    private BigDecimal tienGiamGia;

    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    @Column(name = "tong_tien_sau_giam")
    private BigDecimal tongTienSauGiam;

    @Enumerated(EnumType.STRING)
    @Column(name = "hinh_thuc_thanh_toan")
    private HinhThucThanhToan hinhThucThanhToan;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_hoa_don")
    private LoaiHoaDon loaiHoaDon;

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

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> chiTietHoaDons;
}