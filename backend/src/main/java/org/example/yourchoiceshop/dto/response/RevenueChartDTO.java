package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueChartDTO {
    private String date;             // Ngày (VD: "2026-02-15")
    private BigDecimal revenue;      // Doanh thu trong ngày
    private Long orderCount;         // Số đơn trong ngày
}