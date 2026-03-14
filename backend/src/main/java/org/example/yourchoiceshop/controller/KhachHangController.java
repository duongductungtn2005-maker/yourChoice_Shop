package org.example.yourchoiceshop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.DiaChiKhachHang;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.service.KhachHangService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@CrossOrigin("*")
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final KhachHangRepository khachHangRepository;
    private final ObjectMapper objectMapper;

    // 1) Lấy danh sách
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean gioiTinh,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(
                khachHangService.findAll(
                        keyword, gioiTinh, trangThai,
                        PageRequest.of(page, size, Sort.by("id").descending())
                )
        );
    }

    // 2) Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(khachHangService.findById(id));
    }

    // 3) Check trùng username (hỗ trợ excludeId cho update)
    @GetMapping("/exists-username")
    public ResponseEntity<?> existsUsername(
            @RequestParam String username,
            @RequestParam(required = false) Integer excludeId
    ) {
        try {
            boolean exists = (excludeId == null)
                    ? khachHangService.existsByUsername(username)
                    : khachHangService.existsByUsername(username, excludeId);

            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 4) Check trùng SĐT (hỗ trợ excludeId cho update)
    @GetMapping("/exists-sdt")
    public ResponseEntity<?> existsSoDienThoai(
            @RequestParam String soDienThoai,
            @RequestParam(required = false) Integer excludeId
    ) {
        try {
            boolean exists = khachHangService.existsBySoDienThoai(soDienThoai, excludeId);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 5) Authenticate (đăng nhập)
    @GetMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestParam String username,
            @RequestParam String password
    ) {
        try {
            var customer = khachHangService.getCustomerByCredentials(username, password);
            if (customer == null) {
                return ResponseEntity.ok(Map.of("authenticated", false));
            }

                Map<String, Object> customerData = new LinkedHashMap<>();
                customerData.put("id", customer.getId());
                customerData.put("tenKhachHang", customer.getTenKhachHang());
                customerData.put("email", customer.getEmail());
                customerData.put("soDienThoai", customer.getSoDienThoai());
                customerData.put("tenTaiKhoan", customer.getTenTaiKhoan());
                customerData.put("avatar", customer.getAvatar());
                customerData.put("trangThai", customer.getTrangThai());

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("authenticated", true);
                response.put("customer", customerData);

                return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 6) Tạo mới
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "addresses", required = false) String addressesJson
    ) {
        // Parse addresses JSON (nếu có)
        if (addressesJson != null && !addressesJson.trim().isEmpty()) {
            try {
                List<DiaChiKhachHang> listDiaChi = objectMapper.readValue(
                        addressesJson,
                        new TypeReference<List<DiaChiKhachHang>>() {}
                );
                request.setListDiaChi(listDiaChi);
            } catch (Exception ex) {
                ex.printStackTrace();
                return ResponseEntity.badRequest().body("Địa chỉ không hợp lệ: " + ex.getMessage());
            }
        }

        try {
            return ResponseEntity.ok(khachHangService.create(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 7) Cập nhật
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "addresses", required = false) String addressesJson
    ) {
        // Parse addresses JSON (nếu có)
        if (addressesJson != null && !addressesJson.trim().isEmpty()) {
            try {
                List<DiaChiKhachHang> listDiaChi = objectMapper.readValue(
                        addressesJson,
                        new TypeReference<List<DiaChiKhachHang>>() {}
                );
                request.setListDiaChi(listDiaChi);
            } catch (Exception ex) {
                ex.printStackTrace();
                return ResponseEntity.badRequest().body("Địa chỉ không hợp lệ: " + ex.getMessage());
            }
        }

        try {
            return ResponseEntity.ok(khachHangService.update(id, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 8) Cập nhật trạng thái
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Integer trangThai) {
        try {
            khachHangService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 9) Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.ok("Đã xóa khách hàng");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // 10) Export excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean gioiTinh,
            @RequestParam(required = false) Integer trangThai
    ) throws IOException {
        ByteArrayInputStream in = khachHangService.exportToExcel(keyword, gioiTinh, trangThai);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=khachhang.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    // 11) Thống kê
    @GetMapping("/thong-ke")
    public ResponseEntity<?> getKhachHangThongKe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai
    ) {
        try {
            String searchKey = (keyword != null && !keyword.trim().isEmpty())
                    ? "%" + keyword.trim().toLowerCase() + "%"
                    : "";

            Integer statusFilter = (trangThai != null) ? trangThai : -1;

            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(page, size, Sort.by("id").descending());

            return ResponseEntity.ok(
                    khachHangRepository.searchKhachHangThongKe(searchKey, statusFilter, pageable)
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + ex.getMessage());
        }
    }
}