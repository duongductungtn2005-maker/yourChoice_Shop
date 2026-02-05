package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    // Dòng này giúp lấy được thông tin Sản phẩm cha, nhưng BỎ QUA danh sách con của nó
    // (Tránh lỗi lặp vô tận JSON mà vẫn hiển thị được tên sản phẩm)
    @JsonIgnoreProperties(value = {"chiTietSanPhams", "listChiTietSanPham", "hibernateLazyInitializer", "handler"})
    private SanPham sanPham;

    // --- MÀU SẮC ---
    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    // --- KÍCH THƯỚC ---
    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    // --- CHẤT LIỆU (THÊM MỚI ĐỂ FIX LỖI STARTUP) ---
    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;
    // ----------------------------------------------

    // --- HÌNH ẢNH ---
    @OneToMany(mappedBy = "chiTietSanPham")
    // @JsonIgnore // Có thể mở comment nếu muốn ẩn ảnh để nhẹ JSON
    private List<HinhAnh> hinhAnhs;

    // --- ĐỢT GIẢM GIÁ ---
    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia")
    @JsonIgnore // Ngắt vòng lặp vô tận với Đợt giảm giá
    private DotGiamGia dotGiamGia;
}