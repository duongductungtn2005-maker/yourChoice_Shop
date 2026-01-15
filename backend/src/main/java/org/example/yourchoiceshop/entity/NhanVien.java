package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien extends BaseStatusEntity {
    @Column(name = "ma_nhan_vien") private String maNhanVien;
    @Column(name = "ten_nhan_vien") private String tenNhanVien;
    private String matKhau;
    private String email;
    @Column(name = "so_dien_thoai") private String soDienThoai;
    private Boolean gioiTinh;
    @Column(name = "ngay_sinh") private LocalDate ngaySinh;
    @Column(name = "dia_chi") private String diaChi;

    @ManyToOne @JoinColumn(name = "id_quyen_han") private QuyenHan quyenHan;
}