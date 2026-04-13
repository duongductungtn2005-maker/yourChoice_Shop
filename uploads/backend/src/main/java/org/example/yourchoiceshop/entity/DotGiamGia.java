package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dot_giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGia extends PrimaryEntity {

    @Column(name = "ma_dot_giam_gia")
    private String maDotGiamGia;

    @Column(name = "ten_dot_giam_gia")
    private String tenDotGiamGia;

    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;

    @Column(name = "loai_giam_gia")
    private String loaiGiamGia;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "trang_thai")
    private Integer trangThai;
}