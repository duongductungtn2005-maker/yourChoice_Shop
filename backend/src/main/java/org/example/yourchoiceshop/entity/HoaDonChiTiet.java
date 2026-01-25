package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name = "hoa_don_chi_tiet") @Data
public class HoaDonChiTiet extends PrimaryEntity {
    @Column(name = "ma_hoa_don_chi_tiet") private String maHoaDonChiTiet;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String ghiChu;
    private Integer trangThai;

    @ManyToOne @JoinColumn(name = "id_hoa_don") private HoaDon hoaDon;
    @ManyToOne @JoinColumn(name = "id_chi_tiet_san_pham") private ChiTietSanPham chiTietSanPham;
}