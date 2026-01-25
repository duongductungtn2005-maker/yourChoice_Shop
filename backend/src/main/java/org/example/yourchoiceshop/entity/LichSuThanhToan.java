package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name = "lich_su_thanh_toan") @Data
public class LichSuThanhToan extends PrimaryEntity {
    @ManyToOne @JoinColumn(name = "id_hoa_don") private HoaDon hoaDon;
    private String maGiaoDich;
    private BigDecimal soTien;
    private LocalDateTime ngayThanhToan;
    private String hinhThucThanhToan;
    private String loaiThanhToan;
    private String ghiChu;
    private Integer trangThai;
}