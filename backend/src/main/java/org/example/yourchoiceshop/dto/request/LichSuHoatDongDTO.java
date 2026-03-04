package org.example.yourchoiceshop.dto.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoatDongDTO {
    private Integer id;
    private String tenNhanVien;
    private String maNhanVien;
    private String tenCa;
    private LocalDateTime vaoCaThucTe;
    private LocalDateTime raCaThucTe;
    private Integer trangThai;
    private String ghiChu;
}