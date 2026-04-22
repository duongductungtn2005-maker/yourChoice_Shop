package org.example.yourchoiceshop.controller; // Đổi lại "com.yourproject" cho đúng với project của mày nha

 import org.example.yourchoiceshop.config.StatisticReportScheduler;
// Import cái file chứa hàm gửi mail
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-email")
public class TestEmailController {

    @Autowired
    private StatisticReportScheduler statisticReportScheduler;

    @GetMapping("/send-daily")
    public ResponseEntity<String> testSendDailyEmail() {
        try {
            // Gọi ép cái hàm gửi mail chạy ngay lập tức
            statisticReportScheduler.sendDailyReport();
            return ResponseEntity.ok("Gửi mail thành công! Mày check hòm thư đi.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi rồi: " + e.getMessage());
        }
    }
}