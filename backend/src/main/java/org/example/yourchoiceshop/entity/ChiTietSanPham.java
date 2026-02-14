package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "chi_tiet_san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSanPham extends BaseStatusEntity {

    @Column(name = "ma_ctsp")
    private String maCtsp;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_nhap")
    private BigDecimal giaNhap;

    @Column(name = "gia_ban")
    private BigDecimal giaBan;

    // --- SẢN PHẨM CHA ---
    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    @JsonIgnoreProperties(value = {"chiTietSanPhams", "listChiTietSanPham", "hibernateLazyInitializer", "handler"})
    private SanPham sanPham;

    // --- CÁC THUỘC TÍNH (Đã có) ---
    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    // --- CÁC THUỘC TÍNH (THÊM MỚI ĐỂ FIX LỖI) ---
    @ManyToOne
    @JoinColumn(name = "id_thuong_hieu")
    private ThuongHieu thuongHieu;

    @ManyToOne
    @JoinColumn(name = "id_co_ao")
    private CoAo coAo;

    @ManyToOne
    @JoinColumn(name = "id_tay_ao")
    private TayAo tayAo;

    @ManyToOne
    @JoinColumn(name = "id_xuat_xu")
    private XuatXu xuatXu;
    // ----------------------------------------------

    // --- HÌNH ẢNH ---
    @OneToMany(mappedBy = "chiTietSanPham")
    private List<HinhAnh> hinhAnhs;

    // --- ĐỢT GIẢM GIÁ ---
    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia")
    @JsonIgnore
    private DotGiamGia dotGiamGia;
}