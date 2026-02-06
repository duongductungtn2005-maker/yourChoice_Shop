package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.yourchoiceshop.entity.BaseStatusEntity; // Đảm bảo import đúng đường dẫn Base của bạn

import java.util.List;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPham extends BaseStatusEntity {

    @Column(name = "ma_san_pham")
    private String maSanPham;

    @Column(name = "ten_san_pham")
    private String tenSanPham;

    @Column(name = "mo_ta_chi_tiet")
    private String moTaChiTiet;

    // --- CÁC MỐI QUAN HỆ ---

    @ManyToOne
    @JoinColumn(name = "id_thuong_hieu")
    private ThuongHieu thuongHieu;

    @ManyToOne
    @JoinColumn(name = "id_xuat_xu")
    private XuatXu xuatXu;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @ManyToOne
    @JoinColumn(name = "id_co_ao")
    private CoAo coAo;

    @ManyToOne
    @JoinColumn(name = "id_tay_ao")
    private TayAo tayAo;

    // --- SỬA Ở ĐÂY ---
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore  // <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY
    private List<ChiTietSanPham> chiTietSanPhams;
}