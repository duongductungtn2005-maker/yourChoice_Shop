package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.time.LocalTime;

@Data
public class CaLamViecRequest {
    private String maCa;
    private String tenCa;
    private LocalTime thoiGianBatDau;
    private LocalTime thoiGianKetThuc;
    private String ghiChu;
    private Integer trangThai;
}