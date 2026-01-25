package org.example.yourchoiceshop.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Dùng @Async để việc gửi mail chạy ngầm, không làm đơ giao diện admin
    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = bật chế độ HTML

            mailSender.send(message);
            System.out.println("Đã gửi mail thành công đến: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }
    }
}