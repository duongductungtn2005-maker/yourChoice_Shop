package org.example.yourchoiceshop.controller;

import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.example.yourchoiceshop.dto.request.SendMailRequest;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.example.yourchoiceshop.entity.PhieuGiamGiaCaNhan;
import org.example.yourchoiceshop.repository.PhieuGiamGiaCaNhanRepository;
import org.example.yourchoiceshop.repository.PhieuGiamGiaRepository;
import org.example.yourchoiceshop.service.EmailService;
import org.example.yourchoiceshop.service.impl.PhieuGiamGiaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/phieu-giam-gia")
@CrossOrigin("*")
public class PhieuGiamGiaController {
    @Autowired
    private PhieuGiamGiaCaNhanRepository pggCaNhanRepo; // Inject Repository vừa tạo
    @Autowired
    private PhieuGiamGiaServiceImpl service;

    @Autowired
    private PhieuGiamGiaRepository repository;

    @Autowired
    private EmailService emailService; // Khai báo 1 lần duy nhất

    // 1. API Lấy danh sách (Có lọc nâng cao)
    @GetMapping
    public ResponseEntity<Page<PhieuGiamGia>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String scope) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getAll(keyword, status, scope, pageable));
    }

    // 2. API Tạo mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PhieuGiamGiaRequest req) {
        try {
            return ResponseEntity.ok(service.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 3. API Bật/Tắt & Gia hạn
    // 3. API Bật/Tắt & Gia hạn (ĐÃ SỬA ĐỂ NHẬN ĐƯỢC CỜ GỬI MAIL)
    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            // Nếu Frontend không gửi body lên thì tạo map rỗng để tránh lỗi Null
            if (body == null) {
                body = new java.util.HashMap<>();
            }
            // Đẩy thẳng cả cục Map xuống Service xử lý
            service.toggleStatus(id, body);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 4. API Xuất Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = service.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vouchers.xlsx")
                .body(data);
    }

    // 5. API Gửi Mail
    // 5. API Gửi Mail
    // 5. API Gửi Mail (Đã sửa lại cách nhận Payload)
    @PostMapping("/{id}/send-mail")
    public ResponseEntity<?> sendVoucherEmail(@PathVariable Integer id, @RequestBody Map<String, List<String>> payload) {
        try {
            // 1. Lấy mảng emails từ Payload Map
            List<String> emails = payload.get("emails");
            if (emails == null || emails.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có email nào để gửi!");
            }

            // 2. Tìm phiếu giảm giá
            PhieuGiamGia voucher = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

            String subject = "🎁 Quà tặng từ YourChoice: " + voucher.getTenPhieuGiamGia();
            String senderName = "YourChoice Shop - Khuyến mãi";

            // 3. Chuẩn bị Template HTML
            String htmlBody = """
            <div style="font-family: 'Arial', sans-serif; background:#f1f5f9; padding:30px;">
                <div style="max-width:700px; margin:0 auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(2,6,23,0.06);">
                    
                    <div style="background: linear-gradient(90deg,#0b3b8c,#1e40af); padding:28px 24px; text-align:center; color:#fff;">
                        <h1 style="font-size:22px; margin:6px 0 0; letter-spacing:1px;">
                            🎁 BẠN NHẬN ĐƯỢC MÃ GIẢM GIÁ
                        </h1>
                    </div>

                    <div style="padding:28px 36px; color:#111827;">
                        <p style="margin:0 0 12px;"><strong>Xin chào bạn,</strong></p>

                        <p style="margin:0 0 18px; color:#374151;">
                            Cảm ơn bạn đã đồng hành cùng <strong>YourChoice Shop</strong>.
                            Bạn vừa nhận được một mã giảm giá đặc biệt:
                        </p>

                        <div style="border:2px dashed #0b3b8c; border-radius:10px; padding:20px; text-align:center;">
                            <div style="margin-bottom:15px;">
                                <div style="color:#6b7280; font-size:16px;">Mã voucher của bạn</div>
                                <div style="color:#0b3b8c; font-size:24px; font-weight:700; letter-spacing:2px;">
                                    %s
                                </div>
                            </div>

                            <div style="color:#ef4444; font-size:16px;">
                                Hạn sử dụng đến: <strong>%s</strong>
                            </div>
                        </div>

                        <p style="margin:20px 0 10px; color:#374151;">
                            Hãy đăng nhập và sử dụng ngay để không bỏ lỡ ưu đãi hấp dẫn này!
                        </p>

                        <div style="text-align:center; margin-top:18px;">
                            <a href="http://localhost:5173"
                               style="display:inline-block; background: linear-gradient(90deg,#0b3b8c,#1e40af); color:#fff; padding:12px 26px; border-radius:30px; text-decoration:none; font-weight:600;">
                               MUA SẮM NGAY
                            </a>
                        </div>

                        <p style="margin:20px 0 0; color:#6b7280; font-size:13px;">
                            Trân trọng,<br/>
                            <strong>YourChoice Shop</strong>
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(
                    voucher.getMaPhieuGiamGia(),
                    voucher.getNgayKetThuc() != null ? voucher.getNgayKetThuc().toString() : "Vô thời hạn"
            );

            // 4. Lặp để gửi mail (tao bỏ cái .formatted() bị lỗi Null Pointer của m rồi)
            for (String email : emails) {
                if(email != null && !email.trim().isEmpty()){
                     emailService.sendEmail(email, subject, htmlBody, senderName);
                }
            }

            return ResponseEntity.ok("Đã gửi mail thành công!");

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra màn hình đen để dễ fix nếu còn tịt
            return ResponseEntity.status(500).body("Lỗi hệ thống khi gửi mail: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/customers")
    public ResponseEntity<?> getCustomersByVoucher(@PathVariable Integer id) {
        // 1. Lấy danh sách từ bảng trung gian
        List<PhieuGiamGiaCaNhan> list = pggCaNhanRepo.findByPhieuGiamGiaId(id);

        // 2. Chỉ lấy thông tin khách hàng ra để trả về Frontend
        // Dùng Map để tạo cấu trúc JSON gọn gàng: { id, hoTen, email }
        var result = list.stream().map(item -> {
            var kh = item.getKhachHang();
            return Map.of(
                    "id", kh.getId(),
                    "hoTen", kh.getTenKhachHang(),
                    "email", kh.getEmail());
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 6. API Lấy chi tiết 1 phiếu giảm giá (Dùng cho màn hình chỉnh sửa)
    @GetMapping("/{id}")
    public ResponseEntity<?> getChiTietPhieu(@PathVariable Integer id) {
        // Lấy phiếu giảm giá từ DB
        PhieuGiamGia voucher = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        // Dùng Map để linh hoạt trả về thêm mảng customerIds (nếu là phiếu cá nhân)
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", voucher.getId());
        response.put("maPhieuGiamGia", voucher.getMaPhieuGiamGia());
        response.put("tenPhieuGiamGia", voucher.getTenPhieuGiamGia());
        response.put("loaiPhieu", voucher.getLoaiPhieu());
        response.put("giaTriGiam", voucher.getGiaTriGiam());
        response.put("donHangToiThieu", voucher.getDonHangToiThieu());
        response.put("soLuong", voucher.getSoLuong());
        response.put("kieu", voucher.getKieu());
        response.put("trangThai", voucher.getTrangThai());
        response.put("ngayBatDau", voucher.getNgayBatDau());
        response.put("ngayKetThuc", voucher.getNgayKetThuc());
        response.put("moTa", voucher.getMoTa());
        response.put("giaTriGiamToiDa", voucher.getGiaTriGiamToiDa());
        response.put("gioiHanMoiKhach", voucher.getGioiHanMoiKhach());
        // NẾU là phiếu cá nhân -> Lấy thêm danh sách ID khách hàng để Frontend tích sẵn
        // checkbox
        if ("CaNhan".equals(voucher.getKieu()) || "1".equals(voucher.getKieu())) {
            List<PhieuGiamGiaCaNhan> listKhachHang = pggCaNhanRepo.findByPhieuGiamGiaId(id);
            List<Integer> customerIds = listKhachHang.stream()
                    .map(item -> item.getKhachHang().getId())
                    .collect(Collectors.toList());
            response.put("customerIds", customerIds);
        }

        return ResponseEntity.ok(response);
    }

    // 7. API Cập nhật phiếu giảm giá
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody PhieuGiamGiaRequest req) {
        try {
            // Gọi sang service để xử lý lưu
            return ResponseEntity.ok(service.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}