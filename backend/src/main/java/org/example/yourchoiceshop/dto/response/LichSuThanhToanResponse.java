package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.yourchoiceshop.entity.LichSuThanhToan;

import lombok.*;

@Data
@Getter
@Setter
public class LichSuThanhToanResponse {
    private BigDecimal soTien;
    private LocalDateTime ngayThanhToan;
    private String hinhThucThanhToan;
    private String loaiThanhToan;
    private Integer trangThai;
    private String ghiChu;

    public static LichSuThanhToanResponse fromEntity(LichSuThanhToan entity) {
        LichSuThanhToanResponse res = new LichSuThanhToanResponse();
        res.setSoTien(entity.getSoTien());
        res.setNgayThanhToan(entity.getNgayThanhToan());
        res.setHinhThucThanhToan(entity.getHinhThucThanhToan());
        res.setLoaiThanhToan(entity.getLoaiThanhToan());
        res.setGhiChu(entity.getGhiChu());
        res.setTrangThai(entity.getTrangThai());
        return res;
    }
}
