package org.example.yourchoiceshop.dto.response;

import java.time.LocalDateTime;

import org.example.yourchoiceshop.entity.LichSuHoaDon;

import lombok.*;

@Getter
@Setter
@Data
public class LichSuHoaDonResponse {
    private String hanhDong;
    private LocalDateTime thoiGian;
    private String ghiChu;
    private String tenNhanVien;

    public static LichSuHoaDonResponse fromEntity(LichSuHoaDon entity) {
        LichSuHoaDonResponse res = new LichSuHoaDonResponse();
        res.setHanhDong(entity.getHanhDong());
        res.setThoiGian(entity.getThoiGian());
        res.setGhiChu(entity.getGhiChu());

        if (entity.getNhanVien() != null) {
            res.setTenNhanVien(entity.getNhanVien().getTenNhanVien());
        }

        return res;
    }
}
