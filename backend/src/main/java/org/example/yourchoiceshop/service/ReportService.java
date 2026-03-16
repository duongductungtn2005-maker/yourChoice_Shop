package org.example.yourchoiceshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private HoaDonService hoaDonService;

    public void generateAndSendReport(List<String> emails, LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String timeRange = startTime.format(formatter) + " - " + endTime.format(formatter);
        sendLogic("Báo Cáo Tùy Chỉnh", timeRange, startTime, endTime, emails);
    }

    public void generateAndSendAutoReport(List<String> emails, String reportType, String timeRange, LocalDateTime startTime, LocalDateTime endTime) {
        sendLogic(reportType, timeRange, startTime, endTime, emails);
    }

    // LUỒNG CỐT LÕI
    private void sendLogic(String reportTitle, String timeRangeStr, LocalDateTime start, LocalDateTime end, List<String> emails) {
        try {
            // 1. Doanh thu tổng (Status 5)
            StatisticFilterRequest filterSuccess = new StatisticFilterRequest();
            filterSuccess.setFromDate(start); filterSuccess.setToDate(end); filterSuccess.setStatus(5); 

            var responseSuccess = statisticService.getRevenueStats(filterSuccess);
            long actualRevenue = 0; int successOrders = 0;
            if (responseSuccess != null && responseSuccess.getSummary() != null) {
                actualRevenue = responseSuccess.getSummary().getTotalRevenue() != null ? responseSuccess.getSummary().getTotalRevenue().longValue() : 0;
                successOrders = responseSuccess.getSummary().getTotalOrders() != null ? responseSuccess.getSummary().getTotalOrders().intValue() : 0;
            }

            // 2. Đơn Hủy (Status 0)
            StatisticFilterRequest filterCancel = new StatisticFilterRequest();
            filterCancel.setFromDate(start); filterCancel.setToDate(end); filterCancel.setStatus(0); 
            var responseCancel = statisticService.getRevenueStats(filterCancel);
            int cancelOrders = 0;
            if (responseCancel != null && responseCancel.getSummary() != null) {
                cancelOrders = responseCancel.getSummary().getCancelOrders() != null ? responseCancel.getSummary().getCancelOrders().intValue() : 0;
            }

            // 3. Lấy Hóa Đơn
            Page<HoaDonResponse> pageOrders = hoaDonService.getOrders(null, null, null, start, end, PageRequest.of(0, 999999));
            List<HoaDonResponse> listOrders = pageOrders.getContent();

            // 4. FIX LỖI "TOÀN 0": DÙNG MAP ĐỂ HỨNG DATA TRỰC TIẾP TỪ SQL KHÔNG QUA DTO
            var responseProduct = statisticService.getProductStats(filterSuccess);
            List<Map<String, Object>> listProducts = new ArrayList<>();
            
            if (responseProduct != null && responseProduct.getChartData() != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<?> rawList = (List<?>) responseProduct.getChartData();
                for (Object item : rawList) {
                    try {
                        // Ép thành cặp Key-Value để lôi chính xác từng con số ra
                        Map<String, Object> map = mapper.convertValue(item, Map.class);
                        listProducts.add(map);
                    } catch (Exception ex) {
                        System.err.println("Lỗi chuyển đổi Map: " + ex.getMessage());
                    }
                }
            }

            // 5. Sinh file Excel
            byte[] excelFile = generateExcelReport(actualRevenue, successOrders, cancelOrders, listOrders, listProducts, timeRangeStr);

            // 6. Gửi Mail
            String htmlContent = buildHtmlEmail(reportTitle, timeRangeStr, actualRevenue, successOrders, cancelOrders);
            String subject = "📊 " + reportTitle + " Bán Hàng (" + timeRangeStr + ")";
            String fileName = "BaoCao_ThongKe_" + start.format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) + ".xlsx";

            for (String email : emails) {
                emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent, excelFile, fileName, "Hệ Thống Thống Kê");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========================================================
    // TẠO FILE EXCEL CHUẨN MẪU KHÔNG BAO GIỜ TRƯỢT
    // ========================================================
    private byte[] generateExcelReport(long revenue, int success, int cancel, 
                                       List<HoaDonResponse> orders, 
                                       List<Map<String, Object>> products, 
                                       String timeRange) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN); headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN); headerStyle.setBorderRight(BorderStyle.THIN);
            Font font = workbook.createFont(); font.setBold(true); headerStyle.setFont(font);

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderBottom(BorderStyle.THIN); borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN); borderStyle.setBorderRight(BorderStyle.THIN);

            // --- SHEET 1: TỔNG QUAN ---
            Sheet sheet1 = workbook.createSheet("Tổng Quan Bán Hàng");
            
            Row r0 = sheet1.createRow(1);
            r0.createCell(1).setCellValue("BÁO CÁO DOANH THU HẰNG NGÀY");
            r0.getCell(1).setCellStyle(headerStyle);

            sheet1.createRow(2).createCell(1).setCellValue("Thời gian lấy dữ liệu: " + timeRange);
            sheet1.createRow(3).createCell(1).setCellValue("Tổng Doanh Thu: " + String.format("%,d VNĐ", revenue));
            sheet1.createRow(4).createCell(1).setCellValue("Số đơn hoàn thành: " + success);
            sheet1.createRow(5).createCell(1).setCellValue("Số đơn bị hủy: " + cancel);

            int hr1 = 7;
            Row headerRow1 = sheet1.createRow(hr1);
            String[] cols1 = {"STT", "Mã Hóa Đơn", "Khách Hàng", "Ngày Đặt", "Tổng Tiền", "Trạng Thái"};
            for (int i = 0; i < cols1.length; i++) {
                Cell cell = headerRow1.createCell(i + 1); cell.setCellValue(cols1[i]); cell.setCellStyle(headerStyle);
            }

            int rowNum1 = hr1 + 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            if (orders != null && !orders.isEmpty()) {
                int stt1 = 1;
                for (HoaDonResponse order : orders) {
                    Row row = sheet1.createRow(rowNum1++);
                    row.createCell(1).setCellValue(stt1++);
                    row.createCell(2).setCellValue(order.getMaHoaDon() != null ? order.getMaHoaDon() : "");
                    row.createCell(3).setCellValue(order.getTenKhachHang() != null ? order.getTenKhachHang() : "Khách lẻ");
                    row.createCell(4).setCellValue(order.getNgayTao() != null ? order.getNgayTao().format(dtf) : "");
                    row.createCell(5).setCellValue(order.getTongTienSauGiam() != null ? order.getTongTienSauGiam().doubleValue() : 0);
                    int status = order.getTrangThai() != null ? order.getTrangThai() : -1;
                    row.createCell(6).setCellValue(status == 5 ? "Hoàn thành" : (status == 0 ? "Đã hủy" : "Đang xử lý"));
                    for(int i = 1; i <= 6; i++) { if(row.getCell(i) != null) row.getCell(i).setCellStyle(borderStyle); }
                }
            } else {
                sheet1.createRow(rowNum1).createCell(1).setCellValue("Không có dữ liệu.");
            }
            for (int i = 1; i <= cols1.length; i++) { sheet1.autoSizeColumn(i); }


            // --- SHEET 2: CHI TIẾT SẢN PHẨM ---
            Sheet sheet2 = workbook.createSheet("Chi Tiết Sản Phẩm Bán");
            
            Row titleRow2 = sheet2.createRow(1);
            titleRow2.createCell(1).setCellValue("CHI TIẾT SẢN PHẨM BÁN ĐƯỢC");
            titleRow2.getCell(1).setCellStyle(headerStyle);

            int hr2 = 3;
            Row headerRow2 = sheet2.createRow(hr2);
            // Cột theo file CSV mẫu của mày
            String[] cols2 = {"STT", "Tên Sản Phẩm", "Size / Màu", "Giá (Ước tính)", "Số Lượng", "Thực Thu (Tổng)"};
            for (int i = 0; i < cols2.length; i++) {
                Cell cell = headerRow2.createCell(i + 1); cell.setCellValue(cols2[i]); cell.setCellStyle(headerStyle);
            }

            // --- SHEET 3: VOUCHER & KHÁCH HÀNG ---
Sheet sheet3 = workbook.createSheet("Khuyến Mãi & Khách VIP");
Row headerRow3 = sheet3.createRow(0);
headerRow3.createCell(0).setCellValue("Top Voucher");
headerRow3.createCell(3).setCellValue("Top Khách Hàng");

// Đổ dữ liệu từ statisticService.getVoucherStats() và getCustomerStats() vào đây
// ... (Logic tương tự Sheet 1 và 2 mày đã làm)

            int rowNum2 = hr2 + 1;
            if (products != null && !products.isEmpty()) {
                int stt2 = 1;
                for (Map<String, Object> map : products) {
                    Row row = sheet2.createRow(rowNum2++);
                    row.createCell(1).setCellValue(stt2++);
                    
                    // Lôi từng Key ra, null thì ép về rỗng hoặc 0. Lấy đúng alias trong câu SQL của mày
                    String ten = map.get("tenSanPham") != null ? map.get("tenSanPham").toString() : "";
                    String size = map.get("kichCo") != null ? map.get("kichCo").toString() : "";
                    double soLuong = map.get("soLuongBan") != null ? Double.parseDouble(map.get("soLuongBan").toString()) : 0;
                    double doanhThu = map.get("doanhThu") != null ? Double.parseDouble(map.get("doanhThu").toString()) : 0;
                    
                    row.createCell(2).setCellValue(ten);
                    row.createCell(3).setCellValue(size);
                    
                    // Vì SQL chưa lấy giá từng sản phẩm, lấy tổng tiền / số lượng ra giá trung bình tạm
                    double donGia = (soLuong > 0) ? (doanhThu / soLuong) : 0;
                    row.createCell(4).setCellValue(donGia); 
                    
                    row.createCell(5).setCellValue(soLuong);
                    row.createCell(6).setCellValue(doanhThu);

                    for(int i = 1; i <= 6; i++) { if(row.getCell(i) != null) row.getCell(i).setCellStyle(borderStyle); }
                }
            } else {
                sheet2.createRow(rowNum2).createCell(1).setCellValue("Không có sản phẩm nào.");
            }
            for (int i = 1; i <= cols2.length; i++) { sheet2.autoSizeColumn(i); }

            workbook.write(out);
            return out.toByteArray();
        }
        
    }

    private String buildHtmlEmail(String reportType, String dateString, long totalRevenue, int successOrders, int cancelOrders) {
        String formattedRevenue = String.format("%,d", totalRevenue).replace(",", ".");
        return """
            <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9fafb; color: #374151;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <h2 style="color: #1e3a8a; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;">%s</h2>
                    <p>Kính gửi,</p>
                    <p>Hệ thống xin gửi báo cáo tổng hợp kết quả kinh doanh thời gian: <strong>%s</strong>:</p>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                        <tr style="background-color: #f3f4f6;">
                            <th style="padding: 12px; border: 1px solid #d1d5db; text-align: left;">Chỉ số đo lường</th>
                            <th style="padding: 12px; border: 1px solid #d1d5db; text-align: right;">Kết quả</th>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border: 1px solid #d1d5db;">Doanh thu thực tế (Hoàn thành)</td>
                            <td style="padding: 12px; border: 1px solid #d1d5db; text-align: right; color: #ef4444; font-weight: bold; font-size: 16px;">%s VNĐ</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border: 1px solid #d1d5db;">Đơn hàng hoàn thành</td>
                            <td style="padding: 12px; border: 1px solid #d1d5db; text-align: right; color: #10b981; font-weight: bold;">%d đơn</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border: 1px solid #d1d5db;">Đơn hàng bị hủy</td>
                            <td style="padding: 12px; border: 1px solid #d1d5db; text-align: right; color: #6b7280; font-weight: bold;">%d đơn</td>
                        </tr>
                    </table>
                    
                    <div style="margin-top: 25px; padding: 15px; background-color: #eff6ff; border-left: 5px solid #3b82f6; border-radius: 4px;">
                        <p style="margin: 0; font-size: 14px; color: #1e3a8a; line-height: 1.5;">
                            📎 <strong>File đính kèm:</strong> Chi tiết 2 sheet Hóa Đơn và Sản Phẩm (.xlsx) đã được đính kèm.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(reportType, dateString, formattedRevenue, successOrders, cancelOrders);
    }
}