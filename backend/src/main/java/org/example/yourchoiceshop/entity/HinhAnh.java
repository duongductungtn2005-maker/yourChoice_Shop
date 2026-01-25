package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;

<<<<<<< HEAD
@Entity @Table(name = "hinh_anh") @Data
=======
@Entity
@Table(name = "hinh_anh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class HinhAnh extends PrimaryEntity { // Bảng này trong SQL có trạng thái nhưng ít dùng audit
    @Column(name = "duong_dan_anh") private String duongDanAnh;
    @Column(name = "ten_anh") private String tenAnh;
    @Column(name = "anh_chinh") private Boolean anhChinh;
    @Column(name = "trang_thai") private Integer trangThai;

    @ManyToOne @JoinColumn(name = "id_chi_tiet_san_pham")
    private ChiTietSanPham chiTietSanPham;
}