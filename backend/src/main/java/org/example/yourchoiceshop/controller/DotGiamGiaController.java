package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.example.yourchoiceshop.repository.ChiTietDotGiamGiaRepository;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dot-giam-gia")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DotGiamGiaController {
    private final DotGiamGiaServiceImpl service;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate, // Nhận String rồi parse để tránh lỗi format
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Parse ngày tháng nếu có (Frontend gửi chuỗi ISO)
        LocalDateTime start = (startDate != null && !startDate.isEmpty()) ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty()) ? LocalDateTime.parse(endDate) : null;

        return ResponseEntity.ok(service.getAll(keyword, status, start, end, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DotGiamGiaRequest req) {
        try {
            return ResponseEntity.ok(service.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DotGiamGiaRequest req) {
        try {
            return ResponseEntity.ok(service.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
    }

    // API lấy danh sách sản phẩm trong đợt giảm giá (Cho Modal Xem chi tiết)
    @GetMapping("/{id}/products")
    public ResponseEntity<?> getProducts(@PathVariable Integer id) {
        // Lấy danh sách bảng trung gian
        List<ChiTietDotGiamGia> list = chiTietDotGiamGiaRepository.findByDotGiamGiaId(id);

        // Bạn có thể trả về list này, Frontend sẽ truy cập vào .chiTietSanPham để lấy tên/giá
        return ResponseEntity.ok(list);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = service.exportExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DotGiamGia.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}