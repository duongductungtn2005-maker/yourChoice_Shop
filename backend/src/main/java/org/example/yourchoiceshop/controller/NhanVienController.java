package org.example.yourchoiceshop.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

import jakarta.servlet.http.HttpServletResponse;

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

        @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Lấy đường dẫn gốc của dự án
            String currentPath = System.getProperty("user.dir"); 
            
            // Trỏ vào thư mục uploads/images/nhan-vien/
            Path file = Paths.get(currentPath, "uploads", "images", "nhan-vien").resolve(filename);
            
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        // Thêm dòng này để trình duyệt cache ảnh, load nhanh hơn
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") 
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // Nếu không tìm thấy file, trả về lỗi 404 hoặc ảnh mặc định (tùy chọn)
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
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
    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Boolean gender,
                              @RequestParam(required = false) Integer status) throws IOException {
        
        // 1. Cấu hình Header trả về file
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=NhanVien_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        // 2. Lấy dữ liệu (Cần viết thêm hàm này trong Service nếu chưa có)
        // Lưu ý: Hàm này trả về List<NhanVien>, KHÔNG PHẢI Page<NhanVien>
        List<NhanVien> listNhanVien = nhanVienService.findAllList(keyword, gender, status);

        // 3. Gọi class xuất Excel
        EmployeeExcelExporter excelExporter = new EmployeeExcelExporter(listNhanVien);
        excelExporter.export(response);
    }
}