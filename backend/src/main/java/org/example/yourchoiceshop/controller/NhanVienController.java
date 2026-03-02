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
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@CrossOrigin("*")
public class NhanVienController {

    private static final Logger logger = LoggerFactory.getLogger(NhanVienController.class);
    private final NhanVienService nhanVienService;

    @Autowired
    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    // [UPDATED] Sửa tham số nhận vào: Bỏ gender, thêm role
    @GetMapping
    public ResponseEntity<?> getAllNhanVien(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role // Thêm tham số role
    ) {
        Pageable pageable = PageRequest.of(page, size);
        // Gọi service findAll mới
        Page<NhanVien> result = nhanVienService.findAll(keyword, status, role, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            NhanVien nv = nhanVienService.findById(id);
            return ResponseEntity.ok(nv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không tìm thấy nhân viên: " + e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NhanVien> create(@ModelAttribute EmployeeRequest request) {
        return ResponseEntity.ok(nhanVienService.create(request));
    }

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
            String currentPath = System.getProperty("user.dir");
            Path file = Paths.get(currentPath, "uploads", "images", "nhan-vien").resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable Integer id) {
        try {
            nhanVienService.delete(id);
            return ResponseEntity.ok("Đã chuyển trạng thái nhân viên sang ngừng hoạt động");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

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

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=NhanVien_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<NhanVien> listNhanVien = nhanVienService.findAllList(keyword, gender, status);

        EmployeeExcelExporter excelExporter = new EmployeeExcelExporter(listNhanVien);
        excelExporter.export(response);
    }
    // Đổi đường dẫn ở đây 👇
    @GetMapping("/xac-thuc/tai-khoan") // (Hoặc đường dẫn bạn đang dùng)
public ResponseEntity<Boolean> checkTenTaiKhoan(
        @RequestParam String tenTaiKhoan, 
        @RequestParam(required = false) Integer id) { // CHÚ Ý: Dùng Integer ở đây
    
    boolean isExist = nhanVienService.checkTrungTaiKhoan(tenTaiKhoan, id);
    return ResponseEntity.ok(isExist);
}

    @GetMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean authenticated = nhanVienService.authenticateEmployee(username, password);
        return ResponseEntity.ok(Map.of("authenticated", authenticated));
    }
}