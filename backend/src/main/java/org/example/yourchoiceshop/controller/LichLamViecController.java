package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.yourchoiceshop.dto.request.LichLamViecRequest;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.service.LichLamViecService; 
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.example.yourchoiceshop.repository.GiaoCaRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/v1/lich-lam-viec")
@RequiredArgsConstructor
public class LichLamViecController {

    private final LichLamViecService lichLamViecService;
    private final GiaoCaRepository giaoCaRepo; // Repository để quản lý GiaoCa

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(lichLamViecService.getLichLamViec(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LichLamViecRequest request) {
        lichLamViecService.create(request); 
        return ResponseEntity.ok("Thêm lịch làm việc thành công!"); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        // Gọi hàm mới trong Service đã bao gồm logic đóng ca
        lichLamViecService.delete(id);
        return ResponseEntity.ok("Xóa lịch làm việc và đóng ca thành công (nếu có)");
    }
    
    // API PUT: http://localhost:8080/api/v1/lich-lam-viec/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LichLamViecRequest request) { // Đã đổi Integer -> Long
        lichLamViecService.update(id, request);
        return ResponseEntity.ok("Cập nhật lịch thành công");
    }
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] excelContent = lichLamViecService.generateExcelTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "Template_XepLichNhanVien.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }
        try {
            // Không cần truyền ngày vào nữa
            lichLamViecService.importLichLamViec(file);
            return ResponseEntity.ok("Import dữ liệu thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi đọc file: " + e.getMessage());
        }
    }
    @PostMapping("/copy-last-week")
    public ResponseEntity<?> copyLastWeekSchedule(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String message = lichLamViecService.copyLichTuTuanTruoc(date);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping("/hom-nay")
    public ResponseEntity<?> getLichHomNay(@RequestParam(value = "username", required = false) String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn.");
            }
            
            String decodedUsername = java.net.URLDecoder.decode(username, java.nio.charset.StandardCharsets.UTF_8);
            LichLamViec lich = lichLamViecService.layLichLamViecHomNayCuaNhanVien(decodedUsername);
            
            if (lich == null) {
                // Trả về rỗng kèm mã 200 (vì không có lỗi gì, chỉ là họ được nghỉ thôi)
                return ResponseEntity.ok().build(); 
            }
            
            // Trả về thông tin lịch
            return ResponseEntity.ok(lich);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi lấy lịch hôm nay: " + e.getMessage());
        }
    }
    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        // 1. Lấy thông tin lịch trước khi xóa để biết là của ai
        LichLamViec schedule = lichLamViecService.findById(scheduleId).orElseThrow();
        String username = schedule.getNhanVien().getTenTaiKhoan();

        // 2. Tìm ca làm việc (GiaoCa) đang mở của nhân viên này
        GiaoCa activeShift = giaoCaRepo.findByNhanVienTrongCa_TenTaiKhoanAndTrangThai(username, 1).orElse(null);
        
        // 3. Nếu có ca đang mở, đóng nó lại trước
        if (activeShift != null) {
            activeShift.setTrangThai(0);
            activeShift.setThoiGianGiaoCa(LocalDateTime.now());
            giaoCaRepo.save(activeShift);
        }

        // 4. Cuối cùng mới xóa lịch làm việc
        lichLamViecService.delete(scheduleId);
    }
}