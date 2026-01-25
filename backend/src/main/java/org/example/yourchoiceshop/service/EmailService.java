package org.example.yourchoiceshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // --- 1. Hàm gửi mail chung (Generic & Async) ---
    // Dùng @Async để chạy ngầm, không làm đơ giao diện khi chờ mail gửi đi
    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Cấu hình người gửi
            String senderName = "Hệ thống Quản lý Nhân sự";
            String senderEmail = "phongvth0910@gmail.com"; 

            try {
                helper.setFrom(senderEmail, senderName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(senderEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = bật chế độ HTML

            mailSender.send(message);
            System.out.println("Đã gửi mail thành công đến: " + to);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    // --- 2. Hàm nghiệp vụ cụ thể (Gửi mail tạo tài khoản) ---
    // Hàm này chỉ lo việc tạo nội dung HTML, sau đó gọi hàm sendEmail ở trên để gửi
    public void sendWelcomeEmail(String toEmail, String tenNhanVien, String matKhau) {
        String subject = "THÔNG BÁO TẠO TÀI KHOẢN THÀNH CÔNG";
        
        // Nội dung HTML
        String htmlContent = "<h3>Xin chào " + tenNhanVien + ",</h3>"
                + "<p>Chào mừng bạn gia nhập công ty. Tài khoản của bạn đã được khởi tạo.</p>"
                + "<p>Thông tin đăng nhập hệ thống:</p>"
                + "<ul>"
                + "<li>Email: <b>" + toEmail + "</b></li>"
                + "<li>Mật khẩu tạm thời: <b style='color:red; font-size: 16px;'>" + matKhau + "</b></li>"
                + "</ul>"
                + "<p><i>Vui lòng đổi mật khẩu ngay trong lần đăng nhập đầu tiên để bảo mật thông tin.</i></p>"
                + "<br>"
                + "<p>Trân trọng,<br>Phòng Hành chính - Nhân sự</p>";

        // Gọi hàm gửi mail chung
        sendEmail(toEmail, subject, htmlContent);
    }
}