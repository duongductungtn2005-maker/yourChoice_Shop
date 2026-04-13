package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

    // === Computed fields cho Frontend (không lưu DB) ===

    @JsonProperty("giaSauGiam")
    public BigDecimal getGiaSauGiam() {
        if (dotGiamGia == null || giaBan == null) return null;
        if (dotGiamGia.getTrangThai() == null || dotGiamGia.getTrangThai() != 1) return null;
        LocalDateTime now = LocalDateTime.now();
        if (dotGiamGia.getNgayBatDau() != null && now.isBefore(dotGiamGia.getNgayBatDau())) return null;
        if (dotGiamGia.getNgayKetThuc() != null && now.isAfter(dotGiamGia.getNgayKetThuc())) return null;

        BigDecimal giaTriGiam = dotGiamGia.getGiaTriGiam();
        if (giaTriGiam == null || giaTriGiam.compareTo(BigDecimal.ZERO) <= 0) return null;

        BigDecimal giaSauGiam;
        if ("%".equals(dotGiamGia.getLoaiGiamGia())) {
            giaSauGiam = giaBan.subtract(giaBan.multiply(giaTriGiam).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
        } else {
            giaSauGiam = giaBan.subtract(giaTriGiam);
        }
        return giaSauGiam.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : giaSauGiam;
    }

    @JsonProperty("phanTramGiam")
    public BigDecimal getPhanTramGiam() {
        if (dotGiamGia == null || giaBan == null || giaBan.compareTo(BigDecimal.ZERO) == 0) return null;
        BigDecimal giaSauGiam = getGiaSauGiam();
        if (giaSauGiam == null) return null;
        return giaBan.subtract(giaSauGiam).multiply(BigDecimal.valueOf(100)).divide(giaBan, 0, RoundingMode.HALF_UP);
    }

    @JsonProperty("tenDotGiamGia")
    public String getTenDotGiamGia() {
        if (dotGiamGia == null) return null;
        if (dotGiamGia.getTrangThai() == null || dotGiamGia.getTrangThai() != 1) return null;
        LocalDateTime now = LocalDateTime.now();
        if (dotGiamGia.getNgayBatDau() != null && now.isBefore(dotGiamGia.getNgayBatDau())) return null;
        if (dotGiamGia.getNgayKetThuc() != null && now.isAfter(dotGiamGia.getNgayKetThuc())) return null;
        return dotGiamGia.getTenDotGiamGia();
    }
}