package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDTO {
    private BigDecimal totalRevenue; // Tổng doanh thu
    private Long totalOrders;        // Tổng số đơn
    private BigDecimal averageOrderValue; // Giá trị trung bình/đơn
    private Double growthPercent;    // % tăng trưởng
}