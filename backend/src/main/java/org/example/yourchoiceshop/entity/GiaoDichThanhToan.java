package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_dich_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiaoDichThanhToan extends PrimaryEntity {

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_phuong_thuc_thanh_toan")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "ma_giao_dich_thanh_toan")
    private String maGiaoDichThanhToan;

    @Column(name = "so_tien")
    private BigDecimal soTien;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ma_yeu_cau")
    private String maYeuCau;

    @Column(name = "ma_giao_dich_ngoai")
    private String maGiaoDichNgoai;

    @Column(name = "ma_tham_chieu")
    private String maThamChieu;

    @Column(name = "duong_dan_thanh_toan")
    private String duongDanThanhToan;

    @Column(name = "du_lieu_qr")
    private String duLieuQr;

    @Column(name = "thoi_gian_het_han")
    private LocalDateTime thoiGianHetHan;

    @Column(name = "du_lieu_phan_hoi")
    private String duLieuPhanHoi;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat;

    @Column(name = "ghi_chu")
    private String ghiChu;
}