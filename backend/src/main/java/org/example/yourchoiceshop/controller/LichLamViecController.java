package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import org.example.yourchoiceshop.dto.request.LichLamViecRequest;
import org.example.yourchoiceshop.service.LichLamViecService; 

@RestController
@RequestMapping("/api/v1/lich-lam-viec")
@RequiredArgsConstructor
// MỞ RỘNG CORS: Cho phép tất cả các phương thức, đặc biệt là PUT, DELETE và OPTIONS
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}) 
public class LichLamViecController {

    private final LichLamViecService lichLamViecService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(lichLamViecService.getLichLamViec(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LichLamViecRequest request) {
        lichLamViecService.create(request); 
        return ResponseEntity.ok("Thêm lịch làm việc thành công!"); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        lichLamViecService.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }
    
    // API PUT: http://localhost:8080/api/v1/lich-lam-viec/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LichLamViecRequest request) { // Đã đổi Integer -> Long
        lichLamViecService.update(id, request);
        return ResponseEntity.ok("Cập nhật lịch thành công");
    }
}