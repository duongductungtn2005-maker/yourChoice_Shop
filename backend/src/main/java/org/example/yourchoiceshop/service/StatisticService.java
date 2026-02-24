package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;

import java.util.List;

public interface StatisticService {
    // Hàm xử lý riêng cho báo cáo Doanh Thu
    StandardStatisticResponse<RevenueSummaryDTO, RevenueChartDTO, Object> getRevenueStats(StatisticFilterRequest filter);
    byte[] exportRevenueExcel(StatisticFilterRequest filter);
    // Sau này bạn có thể thêm:
    // StandardStatisticResponse<?, ?, ?> getProductStats(StatisticFilterRequest filter);
    StandardStatisticResponse<Object, Object, ProductStatDTO> getProductStats(StatisticFilterRequest filter);
    StandardStatisticResponse<Object, EmployeeStatDTO, Object> getEmployeeStats(StatisticFilterRequest filter);
    StandardStatisticResponse<Object, CustomerStatDTO, Object> getCustomerStats(StatisticFilterRequest filter);
    StandardStatisticResponse<Object, VoucherStatDTO, Object> getVoucherStats(StatisticFilterRequest filter);
    StandardStatisticResponse<Object, DiscountCampaignStatDTO, Object> getDiscountCampaignStats(StatisticFilterRequest filter);
    // Thêm 2 dòng này vào
    List<OrderStatusDTO> getOrderStatusStats(StatisticFilterRequest filter);

    // 2. Lấy danh sách sản phẩm sắp hết hàng (Tồn kho <= 10)
    StandardStatisticResponse getLowStockStats(StatisticFilterRequest filter);
}



