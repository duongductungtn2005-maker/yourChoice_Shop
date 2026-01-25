package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
<<<<<<< HEAD
@Setter
@Getter
@Entity @Table(name = "phieu_giam_gia") @Data
=======

@Entity 
@Table(name = "phieu_giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class PhieuGiamGia extends PrimaryEntity { // SQL không có audit fields đầy đủ
    @Column(name = "ma_phieu_giam_gia") private String maPhieuGiamGia;
    @Column(name = "ten_phieu_giam_gia") private String tenPhieuGiamGia;
    @Column(name = "loai_phieu") private String loaiPhieu;
    @Column(name = "gia_tri_giam") private BigDecimal giaTriGiam;
    @Column(name = "gia_tri_giam_toi_da") private BigDecimal giaTriGiamToiDa;
    @Column(name = "don_hang_toi_thieu") private BigDecimal donHangToiThieu;
    @Column(name = "so_luong") private Integer soLuong;
    @Column(name = "ngay_bat_dau") private LocalDateTime ngayBatDau;
    @Column(name = "ngay_ket_thuc") private LocalDateTime ngayKetThuc;
<<<<<<< HEAD
    @Column(name = "kieu")
    private String kieu;
=======
>>>>>>> upstream/main
    private Integer trangThai;
}