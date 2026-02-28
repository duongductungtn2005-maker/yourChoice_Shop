package org.example.yourchoiceshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
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

        String htmlContent = """
        <div style="font-family: 'Arial', sans-serif; background:#f1f5f9; padding:30px;">
            <div style="max-width:700px; margin:0 auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(2,6,23,0.06);">
                
                <!-- Header -->
                <div style="background: linear-gradient(90deg,#0b3b8c,#1e40af); padding:28px 24px; text-align:center; color:#fff;">
                    <h1 style="font-size:22px; margin:6px 0 0; letter-spacing:2px;">
                        THÔNG TIN TÀI KHOẢN NHÂN VIÊN
                    </h1>
                </div>

                <!-- Body -->
                <div style="padding:28px 36px; color:#111827;">
                    <p style="margin:0 0 12px;"><strong>Xin chào %s,</strong></p>
                    
                    <p style="margin:0 0 18px; color:#374151;">
                        Chào mừng bạn gia nhập công ty. Tài khoản hệ thống của bạn đã được khởi tạo với thông tin sau:
                    </p>

                    <!-- Info box -->
                    <div style="border:2px dashed #0b3b8c; border-radius:10px; padding:20px; font-size:20px;">
                        <div style="display:block;">
                            
                            <div style="margin-bottom:20px;">
                                <div style="color:#6b7280; font-size:18px;">Email đăng nhập:</div>
                                <div style="color:#0b3b8c; font-size:20px; font-weight:700;">%s</div>
                            </div>
                            
                            <div>
                                <div style="color:#6b7280; font-size:18px;">Mật khẩu:</div>
                                <div style="color:#ef4444; font-size:20px; font-weight:700;">%s</div>
                            </div>
                        </div>
                    </div>

                    <p style="margin:18px 0 6px; color:#374151;">
                        Vui lòng đăng nhập và thay đổi mật khẩu ngay sau lần đăng nhập đầu tiên để đảm bảo bảo mật.
                    </p>

                    <div style="text-align:center; margin-top:18px;">
                        <a href="http://localhost:5173/login"
                           style="display:inline-block; background: linear-gradient(90deg,#0b3b8c,#1e40af); color:#fff; padding:12px 26px; border-radius:30px; text-decoration:none; font-weight:600;">
                           ĐĂNG NHẬP HỆ THỐNG
                        </a>
                    </div>

                    <p style="margin:20px 0 0; color:#6b7280; font-size:13px;">
                        Trân trọng,<br/>
                        <strong>Phòng Hành chính - Nhân sự</strong>
                    </p>
                </div>
            </div>
        </div>
        """.formatted(tenNhanVien, toEmail, matKhau);

        sendEmail(toEmail, subject, htmlContent, senderName);


    }

    // --- 3. Dành cho KHÁCH HÀNG (Thêm mới cho YourChoiceShop) ---
    public void sendCustomerWelcome(String toEmail, String username, String matKhau, String tenKhachHang) {
        String subject = "Chào mừng bạn đến với YourChoiceShop";
        String senderName = "YourChoiceShop"; // Tên người gửi khác đi cho chuyên nghiệp

        // Template HTML theo mẫu coupon (giữ nguyên nội dung: tên đăng nhập và mật khẩu)
        String htmlContent = """
                        <div style="font-family: 'Arial', sans-serif; background:#f1f5f9; padding:30px;">
                            <div style="max-width:700px; margin:0 auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(2,6,23,0.06);">
                                <!-- Header -->
                                <div style="background: linear-gradient(90deg,#0b3b8c,#1e40af); padding:28px 24px; text-align:center; color:#fff;">
                                    <img alt="Logo" src="http://localhost:8080/logo+name.png" style="height:40px; vertical-align:middle; margin-bottom:8px;"/>
                                    <h1 style="font-size:22px; margin:6px 0 0; letter-spacing:2px;">THÔNG TIN ĐĂNG NHẬP</h1>
                                </div>

                                <!-- Body -->
                                <div style="padding:28px 36px; color:#111827;">
                                    <p style="margin:0 0 12px;"><strong>Chào bạn,</strong></p>
                                    <p style="margin:0 0 18px; color:#374151;">Cảm ơn bạn đã đăng ký thành viên tại <strong>YourChoiceShop</strong>. Dưới đây là thông tin đăng nhập của bạn:</p>

                                    <!-- Coupon box (hiển thị thông tin tài khoản; mã khách đã loại bỏ) -->
                                    <div style="border:2px dashed #0b3b8c; border-radius:10px; padding:20px; font-size:20px;">
                                        <div style="display:block;">
                                            <div style="margin-bottom:20px;">
                                                <div style="color:#6b7280; font-size:20px;">Tên tài khoản:</div>
                                                <div style="color:#0b3b8c; font-size:20px; font-weight:700;">%s</div>
                                            </div>
                                            <div>
                                                <div style="color:#6b7280; font-size:20px;">Mật khẩu:</div>
                                                <div style="color:#ef4444; font-size:20px; font-weight:700;">%s</div>
                                            </div>
                                        </div>
                                    </div>

                                    <p style="margin:18px 0 6px; color:#374151;">Bạn có thể đăng nhập bằng thông tin trên để tiếp tục mua sắm và nhận ưu đãi.</p>
                                    <div style="text-align:center; margin-top:18px;">
                                        <a href="http://localhost:5173/login" style="display:inline-block; background: linear-gradient(90deg,#0b3b8c,#1e40af); color:#fff; padding:12px 26px; border-radius:30px; text-decoration:none; font-weight:600;">ĐĂNG NHẬP NGAY</a>
                                    </div>

                                    <p style="margin:20px 0 0; color:#6b7280; font-size:13px;">Trân trọng,<br/><strong>YourChoiceShop</strong></p>
                                </div>
                            </div>
                        </div>
                """.formatted(tenKhachHang, matKhau);

        sendEmail(toEmail, subject, htmlContent, senderName);
    }
    @Autowired
    private JavaMailSender emailSender;

    public void sendStatisticEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("mochimomo0707@gmail.com"); // Thay bằng email gửi đi của hệ thống
        message.setTo(to); 
        message.setSubject(subject); 
        message.setText(text);
        
        emailSender.send(message);
        System.out.println("Đã gửi email báo cáo tới: " + to);
    }

    // --- 4. Dành cho KHÁCH HÀNG: Thông báo phiếu giảm giá ngừng hoạt động ---
    @Async
    public void sendVoucherDeactivatedEmail(String toEmail, String tenKhachHang, String tenVoucher) {
        String subject = "Thông báo: Phiếu giảm giá ngừng hoạt động";
        String senderName = "YourChoiceShop";

        String htmlContent = """
        <div style="font-family: 'Arial', sans-serif; background:#f1f5f9; padding:30px;">
            <div style="max-width:600px; margin:0 auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(2,6,23,0.06);">
                <div style="background: linear-gradient(90deg,#ef4444,#b91c1c); padding:20px 24px; text-align:center; color:#fff;">
                    <h1 style="font-size:20px; margin:0; letter-spacing:1px;">THÔNG BÁO TỪ YOURCHOICESHOP</h1>
                </div>
                <div style="padding:28px 36px; color:#111827;">
                    <p style="margin:0 0 12px; font-size: 16px;"><strong>Chào %s,</strong></p>
                    <p style="margin:0 0 18px; color:#374151; line-height: 1.6;">
                        Chúng tôi rất tiếc phải thông báo rằng phiếu giảm giá <strong>"%s"</strong> mà bạn đang sở hữu hiện đã <strong>ngừng hoạt động</strong>.
                    </p>
                    <p style="margin:0 0 18px; color:#374151; line-height: 1.6;">
                        Bạn sẽ không thể áp dụng phiếu giảm giá này cho các đơn hàng tiếp theo. Đừng buồn nhé, YourChoiceShop vẫn còn rất nhiều chương trình ưu đãi và phiếu giảm giá hấp dẫn khác đang chờ đón bạn.
                    </p>
                    <div style="text-align:center; margin-top:25px;">
                        <a href="http://localhost:5173/products" style="display:inline-block; background: linear-gradient(90deg,#0b3b8c,#1e40af); color:#fff; padding:12px 26px; border-radius:30px; text-decoration:none; font-weight:600;">MUA SẮM NGAY</a>
                    </div>
                    <p style="margin:30px 0 0; color:#6b7280; font-size:13px; border-top: 1px solid #e5e7eb; padding-top: 15px;">
                        Trân trọng,<br/><strong style="color: #0b3b8c; font-size: 14px;">YourChoiceShop Team</strong>
                    </p>
                </div>
            </div>
        </div>
        """.formatted(tenKhachHang != null ? tenKhachHang : "bạn", tenVoucher);

        sendEmail(toEmail, subject, htmlContent, senderName);
    }
}