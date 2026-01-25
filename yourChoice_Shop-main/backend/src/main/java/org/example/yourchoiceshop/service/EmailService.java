package org.example.yourchoiceshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String tenNhanVien, String matKhau) {
        try {
            // 1. Tạo mail có hỗ trợ HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 2. Cấu hình người gửi (Để hiện tên đẹp thay vì chỉ hiện email)
            String senderName = "Hệ thống Quản lý Nhân sự";
            String senderEmail = "phongvth0910@gmail.com"; // Nhớ sửa dòng này thành email thật của bạn
            
            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("THÔNG BÁO TẠO TÀI KHOẢN THÀNH CÔNG");

            // 3. Nội dung HTML (Bôi đậm, xuống dòng đẹp)
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

            helper.setText(htmlContent, true); // true = bật chế độ HTML

            // 4. Gửi mail
            mailSender.send(message);
            System.out.println("Gửi mail thành công cho: " + toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }
}