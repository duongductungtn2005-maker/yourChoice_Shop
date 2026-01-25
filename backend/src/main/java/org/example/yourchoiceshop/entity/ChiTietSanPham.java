package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham extends BaseStatusEntity {

    // TUYỆT ĐỐI KHÔNG CÓ: private Integer id;
    // TUYỆT ĐỐI KHÔNG CÓ: private Integer trangThai;

    @Column(name = "ma_ctsp")
    private String maCtsp;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_nhap")
    private BigDecimal giaNhap;

    @Column(name = "gia_ban")
    private BigDecimal giaBan;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;
    @OneToMany(mappedBy = "chiTietSanPham", fetch = FetchType.LAZY)
    @JsonIgnore // Tránh lỗi vòng lặp vô hạn khi convert JSON
    private List<HinhAnh> hinhAnhs;
}