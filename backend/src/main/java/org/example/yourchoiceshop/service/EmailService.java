package org.example.yourchoiceshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Lấy email từ application.properties (để sau này dễ sửa)
    @Value("${spring.mail.username}")
    private String fromEmail;

    // --- 1. HÀM CỐT LÕI: Gửi mail chung (Giữ nguyên của bạn) ---
    @Async
    public void sendEmail(String to, String subject, String htmlBody, String senderName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, senderName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail thành công đến: " + to);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    // --- 2. Dành cho NHÂN VIÊN (Giữ nguyên logic cũ) ---
    public void sendEmployeeWelcome(String toEmail, String tenNhanVien, String matKhau) {
        String subject = "THÔNG BÁO TẠO TÀI KHOẢN NHÂN VIÊN";
        String senderName = "Hệ thống Quản lý Nhân sự";

        String htmlContent = "<h3>Xin chào " + tenNhanVien + ",</h3>"
                + "<p>Chào mừng bạn gia nhập công ty. Tài khoản của bạn đã được khởi tạo.</p>"
                + "<ul>"
                + "<li>Email: <b>" + toEmail + "</b></li>"
                + "<li>Mật khẩu: <b style='color:red;'>" + matKhau + "</b></li>"
                + "</ul>"
                + "<p>Trân trọng,<br>Phòng Hành chính - Nhân sự</p>";

        sendEmail(toEmail, subject, htmlContent, senderName);
    }

    // --- 3. Dành cho KHÁCH HÀNG (Thêm mới cho SevenStrike) ---
    public void sendCustomerWelcome(String toEmail, String username, String matKhau, String tenKhachHang) {
        String subject = "Chào mừng bạn đến với YourChoiceShop";
        String senderName = "SevenStrike Shop"; // Tên người gửi khác đi cho chuyên nghiệp

        // Nội dung HTML đẹp hơn, phù hợp khách hàng mua sắm
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #0f172a;">Xin chào %s,</h2>
                <p>Cảm ơn bạn đã đăng ký thành viên tại <strong>YourChoiceShop</strong>.</p>
                <p>Dưới đây là thông tin đăng nhập của bạn:</p>
                <div style="background-color: #f8fafc; padding: 15px; border-radius: 8px; border: 1px solid #e2e8f0; width: fit-content;">
                    <p style="margin: 5px 0;"><strong>Tên đăng nhập:</strong> <span style="color: #2563eb;">%s</span></p>
                    <p style="margin: 5px 0;"><strong>Mật khẩu:</strong> <span style="color: #ef4444;">%s</span></p>
                </div>
                <p>Hãy đăng nhập ngay để khám phá các sản phẩm sơ mi nữ mới nhất!</p>
                <p>Trân trọng,<br/><strong>Đội ngũ YourChoiceShop</strong></p>
            </div>
        """.formatted(tenKhachHang, username, matKhau);

        sendEmail(toEmail, subject, htmlContent, senderName);
    }
}