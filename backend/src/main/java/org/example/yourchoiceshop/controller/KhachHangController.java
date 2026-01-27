package org.example.yourchoiceshop.controller; // Lưu ý package

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.KhachHangRequest;
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

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép VueJS gọi API
public class KhachHangController {

    private final KhachHangService khachHangService;

    // 1. Lấy danh sách (Có tìm kiếm + phân trang)
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

    // 3. Tạo mới
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@ModelAttribute KhachHangRequest request) {
        return ResponseEntity.ok(khachHangService.create(request));
    }

    // 4. Cập nhật
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@PathVariable Integer id, @ModelAttribute KhachHangRequest request) {
        return ResponseEntity.ok(khachHangService.update(id, request));
    }

    // 5. Cập nhật trạng thái nhanh (Dùng cho nút Toggle)
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Integer trangThai) {
        khachHangService.updateTrangThai(id, trangThai);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    // 6. Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        khachHangService.delete(id);
        return ResponseEntity.ok("Đã xóa khách hàng");
    }

    // 7. Xuất Excel
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