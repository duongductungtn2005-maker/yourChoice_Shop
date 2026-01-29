package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.service.impl.SanPhamServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SanPhamController {

    private final SanPhamServiceImpl service;

    @GetMapping("/active")
    public ResponseEntity<?> getAllActive() {
        // Service giờ đã trả về List<ProductSimpleResponse>
        return ResponseEntity.ok(service.getAllActive());
    }
}