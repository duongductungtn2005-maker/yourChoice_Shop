package org.example.yourchoiceshop.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.service.NhanVienService;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@CrossOrigin(origins = "http://localhost:5173") 
public class NhanVienController {

    private static final Logger logger = LoggerFactory.getLogger(NhanVienController.class);

    // ĐÃ SỬA: Bỏ @Autowired vì @RequiredArgsConstructor đã lo việc này rồi
    private final NhanVienService nhanVienService;
    @Autowired // Có thể có hoặc không với Spring Boot mới, nhưng nên để cho rõ
    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }
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
        Page<NhanVien> result = nhanVienService.findAll(keyword, gender, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 2. Thêm mới (Create) - Upload File
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NhanVien> create(@ModelAttribute EmployeeRequest request) {
        // @ModelAttribute là bắt buộc để hứng form-data vừa có Text vừa có File
        return ResponseEntity.ok(nhanVienService.create(request));
    }

    // 3. Cập nhật (Update) - Upload File
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id, 
            @ModelAttribute EmployeeRequest request
    ) {
        try {
            return ResponseEntity.ok(nhanVienService.update(id, request));
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
            nhanVienService.delete(id);
            return ResponseEntity.ok("Đã chuyển trạng thái nhân viên sang ngừng hoạt động");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 6. Cập nhật trạng thái nhanh
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id, 
            @RequestParam Integer trangThai 
    ) {
        try {
            nhanVienService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}