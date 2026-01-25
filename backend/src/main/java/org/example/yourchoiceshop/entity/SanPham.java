package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPham extends BaseStatusEntity {
    // id INT PRIMARY KEY IDENTITY(1,1),
    // ma_san_pham VARCHAR(50) UNIQUE,
    // ten_san_pham NVARCHAR(255),
    // mo_ta_chi_tiet NVARCHAR(MAX),
    // id_thuong_hieu INT FOREIGN KEY REFERENCES thuong_hieu(id),
    // id_xuat_xu INT FOREIGN KEY REFERENCES xuat_xu(id),
    // id_chat_lieu INT FOREIGN KEY REFERENCES chat_lieu(id),
    // id_co_ao INT FOREIGN KEY REFERENCES co_ao(id),
    // id_tay_ao INT FOREIGN KEY REFERENCES tay_ao(id),
    // ngay_tao DATETIME,
    // ngay_cap_nhat DATETIME,
    // nguoi_tao NVARCHAR(100),
    // nguoi_cap_nhat NVARCHAR(100),
    // trang_thai INT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ma_san_pham", unique = true)
    private String maSanPham;
    @Column(name = "ten_san_pham")
    private String tenSanPham;
    @Column(name = "mo_ta_chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String moTaChiTiet;
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
    @OneToMany(mappedBy = "sanPham")
    private List<ChiTietSanPham> chiTietSanPhams;
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    @Column(name = "nguoi_tao")
    private String nguoiTao;
    @Column(name = "nguoi_cap_nhat")
    private String nguoiCapNhat;
    @Column(name = "trang_thai")
    private Integer trangThai;
}