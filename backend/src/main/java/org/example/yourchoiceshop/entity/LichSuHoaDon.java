package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name = "lich_su_hoa_don") @Data
public class LichSuHoaDon extends PrimaryEntity {
    private String hanhDong;
    private LocalDateTime thoiGian;
    private String ghiChu;
    private Integer trangThai;
    @ManyToOne @JoinColumn(name = "id_hoa_don") private HoaDon hoaDon;
    @ManyToOne @JoinColumn(name = "id_nhan_vien") private NhanVien nhanVien;
}