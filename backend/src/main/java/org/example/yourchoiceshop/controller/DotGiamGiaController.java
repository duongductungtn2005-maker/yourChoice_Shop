package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.service.impl.DotGiamGiaServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dot-giam-gia")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép Frontend gọi API không bị chặn CORS
public class DotGiamGiaController {

    private final DotGiamGiaServiceImpl service;

    // 1. Lấy danh sách (Hỗ trợ Search + Filter Ngày + Status)
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate, // Nhận String để parse thủ công cho an toàn
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Parse ngày tháng từ String (Frontend gửi lên) sang LocalDateTime
        LocalDateTime start = (startDate != null && !startDate.isEmpty()) ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty()) ? LocalDateTime.parse(endDate) : null;

        return ResponseEntity.ok(service.getAll(keyword, status, start, end, pageable));
    }

    // 2. Lấy chi tiết đợt giảm giá (Cho trang Update nếu cần)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // 3. Tạo mới đợt giảm giá
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DotGiamGiaRequest req) {
        try {
            return ResponseEntity.ok(service.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 4. Cập nhật đợt giảm giá
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DotGiamGiaRequest req) {
        try {
            return ResponseEntity.ok(service.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 5. Kết thúc đợt giảm giá (Xóa mềm / Stop)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã kết thúc đợt giảm giá thành công"));
    }

    // 6. API QUAN TRỌNG: Lấy danh sách sản phẩm cho Modal Xem chi tiết
    @GetMapping("/{id}/products")
    public ResponseEntity<?> getProducts(@PathVariable Integer id) {
        // Trả về List DTO thay vì Entity
        return ResponseEntity.ok(service.getProductsBySaleId(id));
    }

    // 7. Xuất Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = service.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DotGiamGia.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}