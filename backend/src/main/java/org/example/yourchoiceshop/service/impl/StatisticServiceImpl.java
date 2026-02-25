package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;
import org.example.yourchoiceshop.repository.StatisticRepository;
import java.io.ByteArrayOutputStream;
import org.example.yourchoiceshop.service.StatisticService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor

public class StatisticServiceImpl implements StatisticService {

    private final StatisticRepository statisticRepository;

    @Override
    public StandardStatisticResponse getRevenueStats(StatisticFilterRequest filter) {
        StandardStatisticResponse response = new StandardStatisticResponse();

        // 1. Gọi Repo lấy cục Summary
        RevenueSummaryDTO summary = statisticRepository.getRevenueSummary(filter);
        response.setSummary(summary);

        // 2. Gọi Repo lấy cục Chart (danh sách từng ngày)
        List<RevenueChartDTO> chartList = statisticRepository.getRevenueChart(filter);
        response.setChartData(chartList); // Nhét mảng ngày/tháng vào chartData

        // 3. Detail table (nếu chưa có thì để null như Frontend yêu cầu)
        response.setDetailTable(null);

        return response;
    }
    @Override
    public byte[] exportRevenueExcel(StatisticFilterRequest filter) {
        // Lấy lại data biểu đồ để xuất ra từng dòng trong Excel
        List<RevenueChartDTO> dataList = statisticRepository.getRevenueChart(filter);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Báo Cáo Doanh Thu");

            // Tạo Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"STT", "Ngày", "Doanh Thu (VNĐ)", "Số Đơn Hàng"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Đổ dữ liệu
            int rowIdx = 1;
            for (RevenueChartDTO item : dataList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1); // STT
                row.createCell(1).setCellValue(item.getDate());
                row.createCell(2).setCellValue(item.getValue().doubleValue());
//                row.createCell(3).setCellValue(item.getOrderCount());
            }

            // Căn chỉnh độ rộng cột tự động cho đẹp
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo file Excel Doanh Thu: " + e.getMessage());
        }
    }
    @Override
    public StandardStatisticResponse getProductStats(StatisticFilterRequest filter) {
        // 1. Gọi DB để lấy mảng danh sách sản phẩm
        List<ProductStatDTO> productList = statisticRepository.getProductStats(filter);

        // 2. Tạo cục JSON tổng và nhét mảng vào key chartData
        StandardStatisticResponse response = new StandardStatisticResponse();
        response.setChartData(productList);

        return response;
    }
    @Override
    public StandardStatisticResponse<Object, EmployeeStatDTO, Object> getEmployeeStats(StatisticFilterRequest filter) {
        List<EmployeeStatDTO> employeeList = statisticRepository.getEmployeeStats(filter);

        return StandardStatisticResponse.<Object, EmployeeStatDTO, Object>builder()
                .summary(null)
                .chartData(employeeList) // Gắn list nhân viên vào chartData để trả về mảng
                .detailTable(null)
                .build();
    }
    @Override
    public StandardStatisticResponse<Object, CustomerStatDTO, Object> getCustomerStats(StatisticFilterRequest filter) {
        List<CustomerStatDTO> customerList = statisticRepository.getCustomerStats(filter);

        return StandardStatisticResponse.<Object, CustomerStatDTO, Object>builder()
                .summary(null)
                .chartData(customerList)
                .detailTable(null)
                .build();
    }
    @Override
    public StandardStatisticResponse<Object, VoucherStatDTO, Object> getVoucherStats(StatisticFilterRequest filter) {
        List<VoucherStatDTO> voucherList = statisticRepository.getVoucherStats(filter);

        return StandardStatisticResponse.<Object, VoucherStatDTO, Object>builder()
                .summary(null)
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
        // Truyền filter xuống Repository
        List<ProductStatDTO> lowStockList = statisticRepository.getLowStockStats(filter);

        // Bọc vào chuẩn JSON cho Frontend
        StandardStatisticResponse response = new StandardStatisticResponse();
        response.setChartData(lowStockList); // Chú ý: Dùng setData hay setChartData tùy vào code chuẩn của mày

        return response;
    }
}