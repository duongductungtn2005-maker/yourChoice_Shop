package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity @Table(name = "chi_tiet_san_pham") @Data
public class ChiTietSanPham extends BaseStatusEntity {
    @Column(name = "ma_ctsp") private String maCtsp;
    private Integer soLuong;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;

    @ManyToOne @JoinColumn(name = "id_san_pham") private SanPham sanPham;
    @ManyToOne @JoinColumn(name = "id_mau_sac") private MauSac mauSac;
    @ManyToOne @JoinColumn(name = "id_kich_thuoc") private KichThuoc kichThuoc;

    @OneToMany(mappedBy = "chiTietSanPham") private List<HinhAnh> hinhAnhs;
}