package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "dia_chi_khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiKhachHang extends PrimaryEntity {
    @Column(name = "ma_dia_chi") private String maDiaChi;
    @Column(name = "ten_nguoi_nhan") private String tenNguoiNhan;
    @Column(name = "so_dien_thoai") private String soDienThoai;
    private String thanhPho;
    private String quan;
    private String phuong;
    @Column(name = "dia_chi_cu_the") private String diaChiCuThe;
    private Boolean macDinh;
    private Integer trangThai;
    @ManyToOne @JoinColumn(name = "id_khach_hang") private KhachHang khachHang;
}