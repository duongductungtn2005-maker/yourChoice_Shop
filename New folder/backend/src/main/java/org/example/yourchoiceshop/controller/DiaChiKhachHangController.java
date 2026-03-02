package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DiaChiRequest;
import org.example.yourchoiceshop.service.DiaChiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dia-chi")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép Frontend VueJS gọi API
public class DiaChiKhachHangController {

    private final DiaChiService diaChiService;

    // 1. Lấy danh sách địa chỉ của 1 khách hàng
    // URL: /api/v1/dia-chi?khachHangId=1
    @GetMapping
    public ResponseEntity<?> getAllByCustomer(@RequestParam Integer khachHangId) {
        return ResponseEntity.ok(diaChiService.getAllByKhachHangId(khachHangId));
    }

    // 2. Thêm mới địa chỉ
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DiaChiRequest request) {
        return ResponseEntity.ok(diaChiService.create(request));
    }

    // 3. Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DiaChiRequest request) {
        return ResponseEntity.ok(diaChiService.update(id, request));
    }

    // 4. Xóa địa chỉ (Xóa mềm)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        diaChiService.delete(id);
        return ResponseEntity.ok("Đã xóa địa chỉ thành công");
    }

    // 5. Đặt làm mặc định nhanh
    @PutMapping("/{id}/set-default")
    public ResponseEntity<?> setDefault(@PathVariable Integer id) {
        diaChiService.setDefault(id);
        return ResponseEntity.ok("Đã đặt làm địa chỉ mặc định");
    }
}