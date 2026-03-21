package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;
import org.example.yourchoiceshop.repository.StatisticRepository;
import org.example.yourchoiceshop.service.StatisticService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final StatisticRepository statisticRepository;

    @Override
    public StandardStatisticResponse getRevenueStats(StatisticFilterRequest filter) {
        StandardStatisticResponse response = new StandardStatisticResponse();

        // 1. Gọi Repo lấy cục Summary kỳ hiện tại
        RevenueSummaryDTO summary = statisticRepository.getRevenueSummary(filter);
        
        // 2. Logic tính % Tăng trưởng so với kỳ trước (nếu có chọn ngày)
        if (filter.getFromDate() != null && filter.getToDate() != null && summary != null && summary.getTotalRevenue() != null) {
            StatisticFilterRequest previousFilter = new StatisticFilterRequest();
            long days = Duration.between(filter.getFromDate(), filter.getToDate()).toDays() + 1; 
            
            previousFilter.setFromDate(filter.getFromDate().minusDays(days));
            previousFilter.setToDate(filter.getFromDate().minusSeconds(1)); 
            
            RevenueSummaryDTO previousSummary = statisticRepository.getRevenueSummary(previousFilter);
            
            if (previousSummary != null && previousSummary.getTotalRevenue() != null 
                && previousSummary.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
                
                double growth = (summary.getTotalRevenue().doubleValue() - previousSummary.getTotalRevenue().doubleValue()) 
                              / previousSummary.getTotalRevenue().doubleValue() * 100;
                              
                // Đã cập nhật để dùng kiểu Double chuẩn theo DTO của bạn
                summary.setGrowthPercent(growth); 
            } else {
                summary.setGrowthPercent(0.0);
            }
        }
        
        response.setSummary(summary);

        // 3. Gọi Repo lấy cục Chart (danh sách từng ngày)
        List<RevenueChartDTO> chartList = statisticRepository.getRevenueChart(filter);
        response.setChartData(chartList);

        // 4. Detail table
        response.setDetailTable(null);

        return response;
    }

    @Override
    public byte[] exportRevenueExcel(StatisticFilterRequest filter) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // === SHEET 1: BÁO CÁO DOANH THU ===
            Sheet sheet1 = workbook.createSheet("Báo Cáo Doanh Thu");
            String[] cols1 = {"STT", "Ngày", "Doanh Thu (VNĐ)"};
            drawHeaderRow(workbook, sheet1, cols1);

            List<RevenueChartDTO> revenueData = statisticRepository.getRevenueChart(filter);
            int rowIdx = 1;
            for (RevenueChartDTO item : revenueData) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1); // STT
                row.createCell(1).setCellValue(item.getDate());
                row.createCell(2).setCellValue(item.getValue() != null ? item.getValue().doubleValue() : 0);
            }
            autoSizeColumns(sheet1, cols1.length);

            // === SHEET 2: TOP SẢN PHẨM BÁN CHẠY ===
            Sheet sheet2 = workbook.createSheet("Top Sản Phẩm");
            String[] cols2 = {"STT", "Tên Sản Phẩm", "Số Lượng Bán", "Doanh Thu Ước Tính"};
            drawHeaderRow(workbook, sheet2, cols2);

            List<ProductStatDTO> productData = statisticRepository.getProductStats(filter);
            rowIdx = 1;
            for (ProductStatDTO p : productData) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(p.getTenSanPham());
                row.createCell(2).setCellValue(p.getSoLuongBan() != null ? p.getSoLuongBan() : 0);
                row.createCell(3).setCellValue(p.getDoanhThu() != null ? p.getDoanhThu().doubleValue() : 0);
            }
            autoSizeColumns(sheet2, cols2.length);

            // === SHEET 3: HIỆU QUẢ VOUCHER ===
            Sheet sheet3 = workbook.createSheet("Thống Kê Voucher");
            String[] cols3 = {"STT", "Mã Voucher", "Tên Voucher", "Số Lượt Dùng", "Tổng Tiền Đã Giảm"};
            drawHeaderRow(workbook, sheet3, cols3);

            List<VoucherStatDTO> voucherData = statisticRepository.getVoucherStats(filter);
            rowIdx = 1;
            for (VoucherStatDTO v : voucherData) {
                Row row = sheet3.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                
                // Đã cập nhật khớp với getter của VoucherStatDTO bạn vừa gửi
                row.createCell(1).setCellValue(v.getMaVoucher());
                row.createCell(2).setCellValue(v.getTenVoucher());
                row.createCell(3).setCellValue(v.getSoLuotSuDung() != null ? v.getSoLuotSuDung() : 0);
                row.createCell(4).setCellValue(v.getTongTienGiam() != null ? v.getTongTienGiam().doubleValue() : 0);
            }
            autoSizeColumns(sheet3, cols3.length);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo file Excel Đa Tầng: " + e.getMessage());
        }
    }

    // Hàm phụ trợ: Vẽ Header cho các Sheet Excel
    private void drawHeaderRow(Workbook workbook, Sheet sheet, String[] columns) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    // Hàm phụ trợ: Căn chỉnh độ rộng cột Excel
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @Override
    public StandardStatisticResponse getProductStats(StatisticFilterRequest filter) {
        List<ProductStatDTO> productList = statisticRepository.getProductStats(filter);
        StandardStatisticResponse response = new StandardStatisticResponse();
        response.setChartData(productList);
        return response;
    }

    @Override
    public StandardStatisticResponse<Object, EmployeeStatDTO, Object> getEmployeeStats(StatisticFilterRequest filter) {
        List<EmployeeStatDTO> employeeList = statisticRepository.getEmployeeStats(filter);
        return StandardStatisticResponse.<Object, EmployeeStatDTO, Object>builder()
                .summary(null)
                .chartData(employeeList)
                .detailTable(null)
                .build();
    }

    @Override
    public StandardStatisticResponse<Object, CustomerStatDTO, Object> getCustomerStats(StatisticFilterRequest filter) {
        List<CustomerStatDTO> customerList = statisticRepository.getCustomerStats(filter);
        
        // Tính thêm Summary: Tổng số khách đã mua hàng trong kỳ
        long totalUniqueCustomers = customerList.size();
        
        return StandardStatisticResponse.<Object, CustomerStatDTO, Object>builder()
                .summary(totalUniqueCustomers) 
                .chartData(customerList)
                .detailTable(null)
                .build();
    }

    @Override
    public StandardStatisticResponse<Object, VoucherStatDTO, Object> getVoucherStats(StatisticFilterRequest filter) {
        List<VoucherStatDTO> voucherList = statisticRepository.getVoucherStats(filter);

        // Tính tổng số tiền shop đã giảm giá thông qua Voucher trong kỳ
        BigDecimal totalDiscount = voucherList.stream()
                .map(v -> v.getTongTienGiam() != null ? v.getTongTienGiam() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StandardStatisticResponse.<Object, VoucherStatDTO, Object>builder()
                .summary(totalDiscount)
                .chartData(voucherList)
                .detailTable(null)
                .build();
    }

    @Override
    public StandardStatisticResponse<Object, DiscountCampaignStatDTO, Object> getDiscountCampaignStats(StatisticFilterRequest filter) {
        List<DiscountCampaignStatDTO> campaignList = statisticRepository.getDiscountCampaignStats(filter);
        return StandardStatisticResponse.<Object, DiscountCampaignStatDTO, Object>builder()
                .summary(null)
                .chartData(campaignList)
                .detailTable(null)
                .build();
    }

    @Override
    public List<OrderStatusDTO> getOrderStatusStats(StatisticFilterRequest filter) {
        return statisticRepository.getOrderStatusStats(filter);
    }

    @Override
    public StandardStatisticResponse getLowStockStats(StatisticFilterRequest filter) {
        List<ProductStatDTO> lowStockList = statisticRepository.getLowStockStats(filter);
        StandardStatisticResponse response = new StandardStatisticResponse();
        response.setChartData(lowStockList);
        return response;
    }
}