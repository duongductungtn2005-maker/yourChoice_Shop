package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuThanhToan extends PrimaryEntity {

    // LƯU Ý: Đã xóa trường 'id' ở đây vì class cha 'PrimaryEntity' đã có rồi.
    // Nếu để lại sẽ gây lỗi "Duplicate identifier".

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    @JsonIgnore // Ngắt vòng lặp JSON
    private HoaDon hoaDon;

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "so_tien")
    private BigDecimal soTien;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;

    @Column(name = "loai_thanh_toan")
    private String loaiThanhToan;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai;
}