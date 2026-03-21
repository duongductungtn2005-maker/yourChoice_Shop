package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "lich_su_hoa_don") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDon extends PrimaryEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // hanh_dong NVARCHAR(255),
    // thoi_gian DATETIME,
    // ghi_chu NVARCHAR(MAX),
    // id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    // id_nhan_vien INT FOREIGN KEY REFERENCES nhan_vien(id),
    // trang_thai INT
    @Column(name = "hanh_dong")
    private String hanhDong;
    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
    @Column(name = "trang_thai")
    private Integer trangThai;
}
