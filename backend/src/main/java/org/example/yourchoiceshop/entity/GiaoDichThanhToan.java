package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name = "giao_dich_thanh_toan") @Data
=======
import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "giao_dich_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class GiaoDichThanhToan extends PrimaryEntity {
    @ManyToOne @JoinColumn(name = "id_hoa_don") private HoaDon hoaDon;
    @ManyToOne @JoinColumn(name = "id_phuong_thuc_thanh_toan") private PhuongThucThanhToan phuongThucThanhToan;
    @Column(name = "ma_giao_dich_thanh_toan") private String maGiaoDichThanhToan;
    private BigDecimal soTien;
    private Integer trangThai;
    private String maYeuCau;
    private String maGiaoDichNgoai;
    private String maThamChieu;
    private String duongDanThanhToan;
    private String duLieuQr;
    private LocalDateTime thoiGianHetHan;
    private String duLieuPhanHoi;
    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianCapNhat;
    private String ghiChu;
}