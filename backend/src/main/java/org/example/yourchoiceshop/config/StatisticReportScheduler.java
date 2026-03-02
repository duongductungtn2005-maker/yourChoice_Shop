package org.example.yourchoiceshop.config;

import org.example.yourchoiceshop.service.EmailService;
import org.example.yourchoiceshop.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

@Component
public class StatisticReportScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StatisticService statisticService; // T đã mở comment để gọi DB thật

    // Điền email của sếp hoặc email nhận báo cáo vào đây
    private final String TARGET_EMAIL = "mail_nhan_cua_may@gmail.com"; 

    // ==========================================
    // 1. BÁO CÁO HÀNG NGÀY (Chạy lúc 17h00 mỗi ngày)
    // ==========================================
    @Scheduled(cron = "0 0 17 * * ?")
    public void sendDailyReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        String dateString = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        sendReport("Báo Cáo Ngày", dateString, startOfDay, endOfDay);
    }

    // ==========================================
    // 2. BÁO CÁO HÀNG THÁNG (Chạy lúc 17h00 ngày cuối cùng của tháng)
    // ==========================================
    @Scheduled(cron = "0 0 17 L * ?")
    public void sendMonthlyReport() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        String dateString = "Tháng " + currentMonth.getMonthValue() + "/" + currentMonth.getYear();
        sendReport("Báo Cáo Tháng", dateString, startOfMonth, endOfMonth);
    }

    // ==========================================
    // 3. BÁO CÁO HÀNG QUÝ (Chạy lúc 17h00 ngày cuối cùng của các tháng 3, 6, 9, 12)
    // ==========================================
    @Scheduled(cron = "0 0 17 L 3,6,9,12 ?")
    public void sendQuarterlyReport() {
        LocalDate today = LocalDate.now();
        int currentQuarter = today.get(IsoFields.QUARTER_OF_YEAR);
        
        // Tính ngày đầu tiên và cuối cùng của quý
        LocalDate startOfQuarterDate = LocalDate.of(today.getYear(), (currentQuarter - 1) * 3 + 1, 1);
        LocalDateTime startOfQuarter = startOfQuarterDate.atStartOfDay();
        LocalDateTime endOfQuarter = YearMonth.from(startOfQuarterDate.plusMonths(2)).atEndOfMonth().atTime(LocalTime.MAX);

        String dateString = "Quý " + currentQuarter + "/" + today.getYear();
        sendReport("Báo Cáo Quý", dateString, startOfQuarter, endOfQuarter);
    }

    // ==========================================
    // 4. BÁO CÁO HÀNG NĂM (Chạy lúc 17h00 ngày 31/12)
    // ==========================================
    @Scheduled(cron = "0 0 17 L 12 ?")
    public void sendYearlyReport() {
        int year = LocalDate.now().getYear();
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);

        String dateString = "Năm " + year;
        sendReport("Báo Cáo Năm", dateString, startOfYear, endOfYear);
    }

    // ==========================================
    // HÀM XỬ LÝ CHUNG LẤY DATA VÀ GỬI MAIL
    // ==========================================
    private void sendReport(String reportType, String dateString, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // LƯU Ý: Mày cần đổi tên hàm getRevenueData và các thuộc tính (.getTotalRevenue...) 
            // cho đúng với cái class DTO thật trong code của mày nhé.
            var data = statisticService.getRevenueData(startDate, endDate); 
            
            long totalRevenue = (data != null) ? data.getTotalRevenue() : 0;
            int successOrders = (data != null) ? data.getSuccessOrders() : 0;
            int cancelOrders = (data != null) ? data.getCancelOrders() : 0;

            String htmlContent = buildHtmlEmail(reportType, dateString, totalRevenue, successOrders, cancelOrders);
            String subject = "📊 " + reportType + " Bán Hàng - " + dateString;
            
            emailService.sendHtmlEmail(TARGET_EMAIL, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi " + reportType + ": " + e.getMessage());
        }
    }

    // ==========================================
    // HÀM RENDER TEMPLATE HTML (Tái sử dụng cho mọi loại báo cáo)
    // ==========================================
    private String buildHtmlEmail(String reportType, String dateString, long totalRevenue, int successOrders, int cancelOrders) {
        String formattedRevenue = String.format("%,d", totalRevenue).replace(",", ".");
        
        return """
            <div style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9fafb; color: #374151;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <h2 style="color: #1e3a8a; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;">%s Thống Kê Bán Hàng</h2>
                    <p>Kính gửi Quản lý,</p>
                    <p>Hệ thống xin gửi báo cáo tổng hợp kết quả kinh doanh thời gian: <strong>%s</strong>:</p>
                    
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                        <tr style="background-color: #f3f4f6;">
                            <th style="padding: 12px; border: 1px solid #d1d5db; text-align: left;">Chỉ số đo lường</th>
                            <th style="padding: 12px; border: 1px solid #d1d5db; text-align: right;">Kết quả</th>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border: 1px solid #d1d5db;">Tổng doanh thu</td>
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
                    
                    <p style="margin-top: 30px; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email gửi tự động từ hệ thống YourChoice Shop. Vui lòng không trả lời email này.
                    </p>
                </div>
            </div>
            """.formatted(reportType, dateString, formattedRevenue, successOrders, cancelOrders);
    }
}