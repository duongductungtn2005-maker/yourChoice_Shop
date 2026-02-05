package org.example.yourchoiceshop.controller;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chi-tiet-san-pham") // Đường dẫn khớp với Frontend gọi
@CrossOrigin("*")
public class ChiTietSanPhamController {

    @Autowired
    private ChiTietSanPhamRepository repository;

    // API Lấy danh sách biến thể sản phẩm
    @GetMapping
    public ResponseEntity<Page<ChiTietSanPham>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size // Mặc định lấy 100 dòng như Frontend yêu cầu
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(repository.findAll(pageable));
    }
}