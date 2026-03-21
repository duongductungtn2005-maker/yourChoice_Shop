package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.example.yourchoiceshop.service.GiaoCaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/giao-ca")
@RequiredArgsConstructor 
public class GiaoCaController {
    private final NhanVienRepository nhanVienRepo;
    private final GiaoCaService giaoCaService;

    private Integer getUserId(String username) {
    if (username == null || username.trim().isEmpty()) {
        throw new RuntimeException("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn.");
    }

    NhanVien nv = nhanVienRepo.findByTenTaiKhoan(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + username));
    return nv.getId();
    }

    /**
     * 1. Lấy ca làm việc ĐANG MỞ
     */
    @GetMapping("/hien-tai")
    public ResponseEntity<?> getCurrentActiveShift(
            @RequestParam(value = "username", required = false) String username) { 
        try {
            Integer currentUserId = getUserId(username);
            Optional<GiaoCa> activeShift = giaoCaService.getCurrentActiveShift(currentUserId);
            
            if (activeShift.isPresent()) {
                return ResponseEntity.ok(activeShift.get());
            } else {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * 2. Bấm nút "Bắt đầu làm việc (Mở ca)"
     */
    @PostMapping("/mo-ca")
    public ResponseEntity<?> openShift(
            @RequestBody Map<String, Integer> payload, 
            @RequestParam(value = "username", required = false) String username) { 
        try {
            Integer idLichLamViec = payload.get("idLichLamViec");
            if (idLichLamViec == null) {
                return ResponseEntity.badRequest().body("Vui lòng chọn lịch làm việc để mở ca!");
            }

            Integer currentUserId = getUserId(username);
            GiaoCa newShift = giaoCaService.openShift(currentUserId, idLichLamViec);
            return ResponseEntity.ok(newShift);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi mở ca: " + e.getMessage());
        }
    }

    /**
     * 3. Bấm nút "Kết thúc ca làm việc (Đóng ca)"
     */
    @PutMapping("/dong-ca/{idGiaoCa}")
    public ResponseEntity<?> closeShift(@PathVariable Integer idGiaoCa) {
        try {
            GiaoCa closedShift = giaoCaService.closeShift(idGiaoCa);
            return ResponseEntity.ok(closedShift);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống khi đóng ca: " + e.getMessage());
        }
    }
} 
