package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;


import java.util.List;

public interface StatisticRepository {
    // Hàm lấy số liệu tổng quan (Tổng doanh thu, tổng đơn...)
    RevenueSummaryDTO getRevenueSummary(StatisticFilterRequest filter);

    // Hàm lấy dữ liệu vẽ biểu đồ
    List<RevenueChartDTO> getRevenueChart(StatisticFilterRequest filter);
    List<ProductStatDTO> getProductStats(StatisticFilterRequest filter);
    // Thống kê nhân viên
    List<EmployeeStatDTO> getEmployeeStats(StatisticFilterRequest filter);
    // Thống kê khách hàng
    List<CustomerStatDTO> getCustomerStats(StatisticFilterRequest filter);
    // Thống kê Voucher
    List<VoucherStatDTO> getVoucherStats(StatisticFilterRequest filter);
    // Thống kê Đợt giảm giá (Campaign)
    List<DiscountCampaignStatDTO> getDiscountCampaignStats(StatisticFilterRequest filter);
    List<OrderStatusDTO> getOrderStatusStats(StatisticFilterRequest filter);

    // 2. Lấy danh sách sản phẩm sắp hết hàng (Tồn kho <= 10)
    List<ProductStatDTO> getLowStockStats(StatisticFilterRequest filter);
}