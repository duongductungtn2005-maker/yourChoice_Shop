package org.example.yourchoiceshop.controller;
import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.example.yourchoiceshop.dto.request.SendMailRequest;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/phieu-giam-gia")
@CrossOrigin("*")
public class PhieuGiamGiaController {

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
            @RequestParam(required = false) String scope
    ) {
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
    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body) {
        LocalDateTime newEndDate = null;
        if (body != null && body.get("newEndDate") != null) {
            newEndDate = LocalDateTime.parse(body.get("newEndDate"));
        }
        try {
            service.toggleStatus(id, newEndDate);
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
    @PostMapping("/{id}/send-mail")
    public ResponseEntity<?> sendVoucherEmail(@PathVariable Integer id, @RequestBody SendMailRequest req) {
        PhieuGiamGia voucher = repository.findById(id).orElseThrow();
        String subject = "🎁 Quà tặng từ YourChoice: " + voucher.getTenPhieuGiamGia();
        String htmlBody = "<h1>Mã giảm giá: " + voucher.getMaPhieuGiamGia() + "</h1><p>Hạn dùng: " + voucher.getNgayKetThuc() + "</p>";

        for (String email : req.getEmails()) {
            emailService.sendEmail(email, subject, htmlBody);
        }
        return ResponseEntity.ok("Đang gửi mail...");
    }
}