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

    @ManyToOne(fetch = FetchType.LAZY) // Đã thêm LAZY
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY) // Đã thêm LAZY
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY) // Đã thêm LAZY
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia phieuGiamGia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_giao_ca")
    private GiaoCa giaoCa;

    // --- Cập nhật quan hệ (Thêm List con) ---

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    private List<HoaDonChiTiet> hoaDonChiTiets;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    private List<LichSuThanhToan> lichSuThanhToans;
}