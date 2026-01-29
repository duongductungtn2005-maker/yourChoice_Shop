package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.*;

@AllArgsConstructor
@Getter
public class LichSuThanhToanResponse {
    private BigDecimal soTien;
    private LocalDateTime ngayThanhToan;
    private String hinhThucThanhToan;
}

