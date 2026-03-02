package org.example.yourchoiceshop.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.DiaChiKhachHang;
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
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@CrossOrigin("*")
public class KhachHangController {

    private final KhachHangService khachHangService;
    // Khởi tạo Jackson ObjectMapper để xử lý JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. Lấy danh sách
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean gioiTinh,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(khachHangService.findAll(keyword, gioiTinh, trangThai, PageRequest.of(page, size, Sort.by("id").descending())));
    }

    // 2. Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(khachHangService.findById(id));
    }

    @GetMapping("/exists-username")
    public ResponseEntity<?> existsUsername(@RequestParam String username) {
        boolean exists = khachHangService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean authenticated = khachHangService.authenticateCustomer(username, password);
        return ResponseEntity.ok(Map.of("authenticated", authenticated));
    }

    // 3. Tạo mới (FIX LỖI ĐỊA CHỈ TẠI ĐÂY)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "addresses", required = false) String addressesJson // <--- Hứng chuỗi JSON
    ) {
        try {
            // Xử lý chuyển đổi thủ công từ JSON String -> List<DiaChi>
            if (addressesJson != null && !addressesJson.isEmpty()) {
                List<DiaChiKhachHang> listDiaChi = objectMapper.readValue(addressesJson, new TypeReference<List<DiaChiKhachHang>>() {});
                request.setListDiaChi(listDiaChi); // Gán vào DTO
            }
            return ResponseEntity.ok(khachHangService.create(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi dữ liệu địa chỉ: " + e.getMessage());
        }
    }

    // 4. Cập nhật (FIX TƯƠNG TỰ)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @ModelAttribute KhachHangRequest request,
            @RequestParam(value = "addresses", required = false) String addressesJson
    ) {
        try {
            if (addressesJson != null && !addressesJson.isEmpty()) {
                List<DiaChiKhachHang> listDiaChi = objectMapper.readValue(addressesJson, new TypeReference<List<DiaChiKhachHang>>() {});
                request.setListDiaChi(listDiaChi);
            }
            return ResponseEntity.ok(khachHangService.update(id, request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi dữ liệu địa chỉ: " + e.getMessage());
        }
    }

    // ... (Giữ nguyên các hàm updateStatus, delete, export bên dưới)
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Integer trangThai) {
        khachHangService.updateTrangThai(id, trangThai);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        khachHangService.delete(id);
        return ResponseEntity.ok("Đã xóa khách hàng");
    }

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
}