package org.example.yourchoiceshop.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.service.NhanVienService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import org.example.yourchoiceshop.dto.request.LoginRequest;
import org.example.yourchoiceshop.security.JwtUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@CrossOrigin("*")
public class NhanVienController {

    private static final Logger logger = LoggerFactory.getLogger(NhanVienController.class);
    private final NhanVienService nhanVienService;
    private final JwtUtil jwtUtil;

    @Autowired
    public NhanVienController(NhanVienService nhanVienService, JwtUtil jwtUtil) {
        this.nhanVienService = nhanVienService;
        this.jwtUtil = jwtUtil;
    }

    // [UPDATED] Bỏ gender, thêm role
    @GetMapping
    public ResponseEntity<?> getAllNhanVien(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role
    ) {
        Pageable pageable = PageRequest.of(page, size);
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
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean gender,
            @RequestParam(required = false) Integer status
    ) throws IOException {

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

    // ✅ Check trùng SĐT (File 1 có, File 2 thiếu)
    @GetMapping("/xac-thuc/sdt")
    public ResponseEntity<Boolean> checkSoDienThoai(
            @RequestParam String soDienThoai,
            @RequestParam(required = false) Integer id
    ) {
        boolean isExist = nhanVienService.checkTrungSoDienThoai(soDienThoai, id);
        return ResponseEntity.ok(isExist);
    }

    // Authenticate (đăng nhập) — POST để tránh lộ password trong URL
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        NhanVien employee = nhanVienService.getEmployeeByCredentials(
                loginRequest.getUsername(), loginRequest.getPassword());
        if (employee != null) {
            // Xác định role từ quyenHan
            String role = "STAFF";
            if (employee.getQuyenHan() != null) {
                int roleId = employee.getQuyenHan().getId();
                String roleName = String.valueOf(employee.getQuyenHan().getTenQuyenHan()).toUpperCase();
                if (roleId == 1 || roleName.contains("ADMIN") || roleName.contains("QUẢN TRỊ")) {
                    role = "ADMIN";
                }
            }

            String token = jwtUtil.generateToken(
                    employee.getId(), employee.getTenTaiKhoan(), role, null);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("authenticated", true);
            response.put("token", token);
            response.put("employee", employee);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }
}