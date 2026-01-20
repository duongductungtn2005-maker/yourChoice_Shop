package org.example.yourchoiceshop.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.service.NhanVienService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@RequiredArgsConstructor // Lombok tự tạo Constructor cho biến final -> Không cần @Autowired nữa
@CrossOrigin(origins = "http://localhost:5173") 
public class NhanVienController {

    private static final Logger logger = LoggerFactory.getLogger(NhanVienController.class);
    private final NhanVienService service;

    // 1. Lấy danh sách + Tìm kiếm + Lọc
    @GetMapping
    public ResponseEntity<?> getAllNhanVien(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean gender,
        @RequestParam(required = false) Integer status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NhanVien> result = service.findAll(keyword, gender, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. Thêm mới (Create)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NhanVien> create(@ModelAttribute EmployeeRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // 3. Cập nhật (Update) -> BÁC ĐANG THIẾU CÁI NÀY
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id, 
            @ModelAttribute EmployeeRequest request
    ) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // 4. Hiển thị ảnh (Serve Image)
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Đường dẫn này PHẢI TRÙNG khớp với đường dẫn trong Service
            Path file = Paths.get("uploads/images/nhan-vien/").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG) // Báo cho trình duyệt biết đây là ảnh
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
            service.delete(id);
            return ResponseEntity.ok("Đã chuyển trạng thái nhân viên sang ngừng hoạt động");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 6. Cập nhật trạng thái nhanh (Toggle Switch)
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id, 
            @RequestParam Integer trangThai 
    ) {
        try {
            service.updateTrangThai(id, trangThai);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}