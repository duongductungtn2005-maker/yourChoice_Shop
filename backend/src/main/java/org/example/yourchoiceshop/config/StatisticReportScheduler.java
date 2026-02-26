package org.example.yourchoiceshop.config;

 import org.example.yourchoiceshop.service.EmailService;
import org.example.yourchoiceshop.service.StatisticService;
// Service lấy số liệu thống kê của mày
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StatisticReportScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StatisticService statisticService; 
    
    // Gửi email báo cáo hàng ngày lúc 17:00:00
    // cron: Giây - Phút - Giờ - Ngày - Tháng - Thứ
    @Scheduled(cron = "0 0 17 * * ?")
    public void sendDailyReport() {
        // 1. Gọi logic lấy số liệu thống kê (ví dụ lấy doanh thu hôm nay)
        // String reportData = statisticService.getDailySummary();
        String reportData = "Doanh thu hôm nay: 10.000.000 VNĐ. Số đơn: 50. (Data mẫu)";
        
        // 2. Lấy danh sách email nhận báo cáo từ Database (hoặc fix cứng tùy logic mày lưu ở frontend)
        String toEmail = "sếp_của_mày@gmail.com"; 
        
        // 3. Gửi
        emailService.sendStatisticEmail(
            toEmail, 
            "Báo Cáo Thống Kê Ngày " + LocalDate.now(), 
            "Dưới đây là báo cáo thống kê bán hàng ngày hôm nay:\n" + reportData
        );
    }

    // Gửi báo cáo hàng tháng lúc 17:00 vào ngày cuối cùng của tháng (L)
    @Scheduled(cron = "0 0 17 L * ?")
    public void sendMonthlyReport() {
        String reportData = "Dữ liệu tổng kết tháng này...";
        emailService.sendStatisticEmail("sếp_của_mày@gmail.com", "Báo Cáo Thống Kê Tháng " + LocalDate.now().getMonthValue(), reportData);
    }

    // Gửi báo cáo hàng quý (Chạy vào ngày 1 của các tháng 1, 4, 7, 10 lúc 17h)
    @Scheduled(cron = "0 0 17 1 1,4,7,10 ?")
    public void sendQuarterlyReport() {
        // Tự móc dữ liệu quý
    }

    // Gửi báo cáo hàng năm (Chạy vào ngày cuối cùng của tháng 12 lúc 17h)
    @Scheduled(cron = "0 0 17 L 12 ?")
    public void sendYearlyReport() {
        // Tự móc dữ liệu năm
    }
}