package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "phieu_giam_gia_ca_nhan") @Data
public class PhieuGiamGiaCaNhan extends PrimaryEntity {
    @Column(name = "ma_phieu_khach_hang") private String maPhieuKhachHang;
    @Column(name = "ngay_nhan") private LocalDateTime ngayNhan;
    @Column(name = "da_su_dung") private Boolean daSuDung;
    private Integer trangThai;
    @ManyToOne @JoinColumn(name = "id_khach_hang") private KhachHang khachHang;
    @ManyToOne @JoinColumn(name = "id_phieu_giam_gia") private PhieuGiamGia phieuGiamGia;
}