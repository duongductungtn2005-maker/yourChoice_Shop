package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;

import java.util.List;

public interface StatisticService {

    // ĐÃ THÊM HÀM NÀY ĐỂ FIX LỖI DÒNG 20
    StandardStatisticResponse getRevenueStats(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, ProductStatDTO, Object>
    getLowStockStats(StatisticFilterRequest filter);

    byte[] exportRevenueExcel(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, Object, ProductStatDTO>
    getProductStats(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, EmployeeStatDTO, Object>
    getEmployeeStats(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, CustomerStatDTO, Object>
    getCustomerStats(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, VoucherStatDTO, Object>
    getVoucherStats(StatisticFilterRequest filter);

    StandardStatisticResponse<Object, DiscountCampaignStatDTO, Object>
    getDiscountCampaignStats(StatisticFilterRequest filter);

    List<OrderStatusDTO>
    getOrderStatusStats(StatisticFilterRequest filter);
}