package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.service.StatisticService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    // Frontend sẽ gọi POST đến /api/v1/statistics/REVENUE
    @PostMapping("/{type}")
    public ResponseEntity<?> getStatistics(
            @PathVariable String type,
            @RequestBody StatisticFilterRequest filter) {

        // Dùng switch-case để rẽ nhánh tùy theo loại thống kê Frontend muốn xem
        return switch (type.toUpperCase()) {
            case "REVENUE" -> ResponseEntity.ok(statisticService.getRevenueStats(filter));
            case "PRODUCT" -> ResponseEntity.ok(statisticService.getProductStats(filter));
            case "EMPLOYEE" -> ResponseEntity.ok(statisticService.getEmployeeStats(filter));
            case "CUSTOMER" -> ResponseEntity.ok(statisticService.getCustomerStats(filter));
            case "VOUCHER" -> ResponseEntity.ok(statisticService.getVoucherStats(filter));
            case "DISCOUNT_CAMPAIGN" -> ResponseEntity.ok(statisticService.getDiscountCampaignStats(filter));
            case "ORDER_STATUS" -> ResponseEntity.ok(statisticService.getOrderStatusStats(filter));
            case "LOW_STOCK" -> ResponseEntity.ok(statisticService.getLowStockStats(filter));
            default -> ResponseEntity.badRequest().body("Loại thống kê không hợp lệ: " + type);
        };
    }

    @PostMapping("/{type}/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable String type,
            @RequestBody StatisticFilterRequest filter) {

        if ("REVENUE".equalsIgnoreCase(type)) {
            byte[] excelData = statisticService.exportRevenueExcel(filter);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BaoCaoDoanhThu.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        }

        return ResponseEntity.badRequest().body(null);
    }
}