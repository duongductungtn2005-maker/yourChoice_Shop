package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StatisticService statisticService;

    // Hàm gọi từ API Gửi Thủ Công
    public void generateAndSendReport(List<String> emails, LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String timeRange = startTime.format(formatter) + " - " + endTime.format(formatter);
        
        sendLogic("Báo Cáo Tùy Chỉnh", timeRange, startTime, endTime, emails);
    }

    // Hàm gọi từ Scheduler (Gửi tự động)
    public void generateAndSendAutoReport(List<String> emails, String reportType, String timeRange, LocalDateTime startTime, LocalDateTime endTime) {
        sendLogic(reportType, timeRange, startTime, endTime, emails);
    }

    // LUỒNG CỐT LÕI: Lấy data thật và gửi
    private void sendLogic(String reportTitle, String timeRangeStr, LocalDateTime start, LocalDateTime end, List<String> emails) {
        try {
            // 1. Gọi API lấy Doanh Thu Thực Tế (Chỉ lấy đơn Hoàn Thành - Status 5)
            StatisticFilterRequest filterSuccess = new StatisticFilterRequest();
            filterSuccess.setFromDate(start);
            filterSuccess.setToDate(end);
            filterSuccess.setStatus(5); // Ép status = 5 để lấy tiền thật

            var responseSuccess = statisticService.getRevenueStats(filterSuccess);
            long actualRevenue = 0;
            int successOrders = 0;

            if (responseSuccess != null && responseSuccess.getSummary() != null) {
                actualRevenue = responseSuccess.getSummary().getTotalRevenue() != null ? responseSuccess.getSummary().getTotalRevenue().longValue() : 0;
                successOrders = responseSuccess.getSummary().getTotalOrders() != null ? responseSuccess.getSummary().getTotalOrders().intValue() : 0;
            }

            // 2. Gọi API lấy Đơn Hủy (Status 0)
            StatisticFilterRequest filterCancel = new StatisticFilterRequest();
            filterCancel.setFromDate(start);
            filterCancel.setToDate(end);
            filterCancel.setStatus(0); // Ép status = 0 để lấy số đơn hủy

            var responseCancel = statisticService.getRevenueStats(filterCancel);
            int cancelOrders = 0;
            if (responseCancel != null && responseCancel.getSummary() != null) {
                cancelOrders = responseCancel.getSummary().getCancelOrders() != null ? responseCancel.getSummary().getCancelOrders().intValue() : 0;
            }

            // 3. Tạo HTML và Bắn Mail
            String htmlContent = buildHtmlEmail(reportTitle, timeRangeStr, actualRevenue, successOrders, cancelOrders);
            String subject = "📊 " + reportTitle + " Bán Hàng (" + timeRangeStr + ")";

            for (String email : emails) {
                emailService.sendHtmlEmail(email, subject, htmlContent);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo và gửi báo cáo: " + e.getMessage());
        }
    }

    // Template HTML xịn sò
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
                    <p style="margin-top: 30px; font-size: 12px; color: #9ca3af; text-align: center;">Đây là email tự động từ YourChoice Shop.</p>
                </div>
            </div>
            """.formatted(reportType, dateString, formattedRevenue, successOrders, cancelOrders);
    }
}