package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.repository.ChiTietDotGiamGiaRepository;
import org.example.yourchoiceshop.service.impl.DotGiamGiaServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/dot-giam-gia")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DotGiamGiaController {
    private final DotGiamGiaServiceImpl service;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DotGiamGiaRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DotGiamGiaRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getProducts(@PathVariable Integer id) {
        // Trả về danh sách sản phẩm nằm trong đợt giảm giá này
        // Lưu ý: Cần entity ChiTietDotGiamGia có quan hệ @ManyToOne với ChiTietSanPham
        return ResponseEntity.ok(chiTietDotGiamGiaRepository.findByDotGiamGiaId(id));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        // Tương tự Voucher, gọi service export
        return ResponseEntity.ok().body(new byte[0]); // Demo
    }
}