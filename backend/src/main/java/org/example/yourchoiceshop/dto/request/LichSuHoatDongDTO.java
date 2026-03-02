package org.example.yourchoiceshop.dto.request;

import java.time.LocalDateTime;

@Data
public class LichSuHoatDongDTO {
    private Integer id;
    private String tenNhanVien;
    private String maNhanVien;
    private String tenCa;
    private LocalDateTime vaoCaThucTe;
    private LocalDateTime raCaThucTe;
    private Integer trangThai; // 1: Đang làm việc, 2: Đã đóng ca
    private String ghiChu;
}