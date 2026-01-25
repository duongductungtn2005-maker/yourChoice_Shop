package org.example.yourchoiceshop.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import gọn hơn

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/khach-hang")
@CrossOrigin(origins = "http://localhost:5173") 
public class KhachHangController {

    private static final Logger logger = LoggerFactory.getLogger(KhachHangController.class);

    // ĐÃ SỬA: Bỏ @Autowired vì @RequiredArgsConstructor đã lo việc này rồi
    private final KhachHangService khachHangService;
    @Autowired // Có thể có hoặc không với Spring Boot mới, nhưng nên để cho rõ
    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }
    // 1. Lấy danh sách + Tìm kiếm + Lọc
    @GetMapping
    public ResponseEntity<?> getAllKhachHang(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean gender,
        @RequestParam(required = false) Integer status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KhachHang> result = khachHangService.findAll(keyword, gender, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. Thêm mới (Create) - Upload File
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KhachHang> create(@ModelAttribute KhachHangRequest request) {
        // @ModelAttribute là bắt buộc để hứng form-data vừa có Text vừa có File
        return ResponseEntity.ok(khachHangService.create(request));
    }

    // 3. Cập nhật (Update) - Upload File
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id, 
            @ModelAttribute KhachHangRequest request
    ) {
        try {
            return ResponseEntity.ok(khachHangService.update(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // 4. Hiển thị ảnh (Serve Image)
    // Cấu trúc {filename:.+} giúp lấy cả đuôi file (.jpg, .png)
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // LƯU Ý: Đường dẫn này phải trùng khớp folder lưu ảnh trong Service
            Path file = Paths.get("uploads/images/nhan-vien/").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG) 
                        .body(resource);
            }
        } catch (MalformedURLException e) {
            logger.error("Error serving file: {}", filename, e);
        }
        return ResponseEntity.notFound().build();
    }

    // 5. Xóa mềm (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable Integer id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.ok("Đã chuyển trạng thái khách hàng sang ngừng hoạt động");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}