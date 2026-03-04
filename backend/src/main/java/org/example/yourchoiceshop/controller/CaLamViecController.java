package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.CaLamViecRequest;
import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.service.CaLamViecService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ca-lam-viec")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API mà không bị lỗi CORS
@RequiredArgsConstructor
public class CaLamViecController {

    private final CaLamViecService caLamViecService;

    // API thêm mới: POST http://localhost:8080/api/ca-lam-viec
    @PostMapping
    public ResponseEntity<CaLamViec> create(@RequestBody CaLamViecRequest request) {
        CaLamViec newCa = caLamViecService.create(request);
        return ResponseEntity.ok(newCa);
    }
    // API Cập nhật trạng thái: PUT http://localhost:8080/api/v1/ca-lam-viec/{id}/trang-thai
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<CaLamViec> updateTrangThai(
            @PathVariable("id") Integer id, 
            @RequestParam("trangThai") Integer trangThai) {
        
        // Gọi service để cập nhật (bạn cần viết thêm hàm này trong CaLamViecService)
        CaLamViec updatedCa = caLamViecService.updateTrangThai(id, trangThai);
        
        return ResponseEntity.ok(updatedCa);
    }
    @GetMapping
    public ResponseEntity<Page<CaLamViec>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            // Ép kiểu giờ từ String (08:30) của thẻ input type="time" sang LocalTime của Java
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CaLamViec> result = caLamViecService.searchAndFilter(keyword, status, startTime, endTime, pageable);
        return ResponseEntity.ok(result);
    }
    // 1. Thêm API lấy chi tiết để đổ dữ liệu vào Form Edit
@GetMapping("/{id}")
public ResponseEntity<CaLamViec> getById(@PathVariable("id") Integer id) {
    // Đảm bảo trong CaLamViecService đã có hàm findById hoặc getById
    CaLamViec ca = caLamViecService.getById(id); 
    return ResponseEntity.ok(ca);
}

// 2. Thêm API cập nhật thông tin ca làm việc
@PutMapping("/{id}")
public ResponseEntity<CaLamViec> update(
        @PathVariable("id") Integer id, 
        @RequestBody CaLamViecRequest request) {
    CaLamViec updatedCa = caLamViecService.update(id, request);
    return ResponseEntity.ok(updatedCa);
}
}