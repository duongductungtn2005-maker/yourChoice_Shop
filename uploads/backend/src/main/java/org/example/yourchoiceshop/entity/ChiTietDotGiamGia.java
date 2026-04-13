package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_dot_giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietDotGiamGia extends BaseStatusEntity {

    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia")
    private DotGiamGia dotGiamGia;

    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham")
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "so_luong_ap_dung")
    private Integer soLuongApDung;

    @Column(name = "gia_tri_giam_rieng")
    private BigDecimal giaTriGiamRieng;

    @Column(name = "so_tien_toi_da_giam_rieng")
    private BigDecimal soTienToiDaGiamRieng;

    @Column(name = "ghi_chu")
    private String ghiChu;
}