package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_ca")
@Data
public class GiaoCa extends BaseStatusEntity {

    @Column(name = "ma_giao_ca")
    private String maGiaoCa;

    // Người trực ca hiện tại (người mở ca)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_trong_ca")
    private NhanVien nhanVienTrongCa;

    // Người nhận ca tiếp theo (người được bàn giao khi đóng ca)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_nhan_ca")
    private NhanVien nhanVienNhanCa;

    @Column(name = "thoi_gian_nhan_ca")
    private LocalDateTime thoiGianNhanCa;

    @Column(name = "thoi_gian_giao_ca")
    private LocalDateTime thoiGianGiaoCa;

    @Column(name = "tien_ban_dau")
    private BigDecimal tienBanDau;

    @Column(name = "tong_thu_trong_ca")
    private BigDecimal tongThuTrongCa;

    @Column(name = "tong_tien_mat")
    private BigDecimal tongTienMat;

    @Column(name = "tong_tien_chuyen_khoan")
    private BigDecimal tongTienChuyenKhoan;

    @Column(name = "tien_phat_sinh")
    private BigDecimal tienPhatSinh;

    @Column(name = "tien_thuc_te_kiem_dem")
    private BigDecimal tienThucTeKiemDem;

    @Column(name = "tien_chenh_lech")
    private BigDecimal tienChenhLech;

    @Column(name = "ly_do_chenh_lech")
    private String lyDoChenhLech;

    @Column(name = "ghi_chu")
    private String ghiChu;
}