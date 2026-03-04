package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalProducts;
    private Long successOrders;
    private Long processingOrders;
    private Long cancelOrders;
    private Long returnOrders;
    private Double growthPercent;
}