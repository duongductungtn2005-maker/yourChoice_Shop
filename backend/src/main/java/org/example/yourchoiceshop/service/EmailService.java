package org.example.yourchoiceshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.yourchoiceshop.entity.PhieuGiamGia; // Nhớ import Entity này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Lấy email từ file cấu hình (application.properties) cho chuẩn
    @Value("${spring.mail.username}")
    private String senderEmail;

    // --- 1. Hàm gửi mail chung (CORE) ---
    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Cấu hình tên hiển thị người gửi
            String senderName = "YourChoice Shop";

            try {
                helper.setFrom(senderEmail, senderName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(senderEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = bật chế độ HTML

            mailSender.send(message);
            System.out.println("✅ Đã gửi mail thành công đến: " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Lỗi gửi mail: " + e.getMessage());
        }
    }

    // --- 2. Hàm nghiệp vụ: Gửi phiếu giảm giá (MỚI THÊM) ---
    // --- Hàm gửi Voucher đẹp (Giống giao diện Web) ---
    // --- Hàm gửi Voucher đẹp (Đã fix lỗi %) ---
    public void sendVoucherEmail(String toEmail, PhieuGiamGia voucher) {
        String subject = "🎁 Quà tặng đặc biệt từ YourChoice!";

        DecimalFormat df = new DecimalFormat("###,###,###");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String giaTri = "PhanTram".equals(voucher.getLoaiPhieu()) 
                ? voucher.getGiaTriGiam() + "%" 
                : df.format(voucher.getGiaTriGiam()) + "đ";
        
        String hanDung = voucher.getNgayKetThuc().format(dtf);

        // LƯU Ý: width: 100% phải viết thành 100%% để tránh lỗi Java
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8fafc; padding: 20px;">
                <h3 style="color: #2b4360; text-align: center;">Chào bạn, YourChoice gửi tặng bạn mã giảm giá!</h3>
                
                <table style="width: 100%%; border-collapse: separate; border-spacing: 0; background-color: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);">
                    <tr>
                        <td style="width: 35%%; background-color: #2b4360; color: #fff; text-align: center; padding: 20px; vertical-align: middle;">
                            <div style="font-size: 28px; font-weight: bold; color: #eddcc3; margin-bottom: 5px;">%s</div>
                            <div style="font-size: 12px; letter-spacing: 1px; text-transform: uppercase;">GIẢM GIÁ</div>
                            <div style="margin-top: 10px;">
                                <span style="background-color: rgba(255,255,255,0.2); padding: 4px 8px; border-radius: 4px; font-size: 10px; font-weight: bold;">RIÊNG TƯ</span>
                            </div>
                        </td>
                        
                        <td style="width: 65%%; padding: 20px; border-left: 2px dashed #cbd5e1; vertical-align: middle;">
                            <h4 style="margin: 0 0 5px 0; color: #334155; font-size: 16px;">%s</h4>
                            <p style="margin: 0 0 15px 0; color: #64748b; font-size: 13px;">Đơn tối thiểu: %s đ</p>
                            
                            <div style="background-color: #f1f5f9; padding: 10px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-family: monospace; font-size: 16px; font-weight: bold; color: #2b4360; letter-spacing: 1px;">%s</span>
                            </div>
                            
                            <p style="margin: 10px 0 0 0; font-size: 12px; color: #94a3b8; text-align: right;">HSD: %s</p>
                        </td>
                    </tr>
                </table>

                <div style="text-align: center; margin-top: 20px; font-size: 12px; color: #888;">
                    <p>Hãy sử dụng mã này tại bước thanh toán. Chúc bạn mua sắm vui vẻ!</p>
                </div>
            </div>
        """.formatted(
                giaTri,
                voucher.getTenPhieuGiamGia(),
                df.format(voucher.getDonHangToiThieu()),
                voucher.getMaPhieuGiamGia(),
                hanDung
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    // --- 3. Hàm nghiệp vụ cũ: Gửi mail nhân sự (GIỮ NGUYÊN) ---
    public void sendWelcomeEmail(String toEmail, String tenNhanVien, String matKhau) {
        String subject = "THÔNG BÁO TẠO TÀI KHOẢN THÀNH CÔNG";
        String htmlContent = "<h3>Xin chào " + tenNhanVien + ",</h3>"
                + "<p>Chào mừng bạn gia nhập công ty...</p>" // (Giữ nguyên nội dung cũ của bạn)
                + "<ul>"
                + "<li>Email: <b>" + toEmail + "</b></li>"
                + "<li>Mật khẩu: <b style='color:red;'>" + matKhau + "</b></li>"
                + "</ul>";

        sendEmail(toEmail, subject, htmlContent);
    }
}