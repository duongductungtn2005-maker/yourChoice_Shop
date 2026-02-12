package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.MauSac;
import org.example.yourchoiceshop.service.impl.MauSacServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.example.yourchoiceshop.repository.MauSacRepository; // Import Repo
@RestController
@RequestMapping("/api/v1/mau-sac")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MauSacController {
    private final MauSacServiceImpl service;
    private final MauSacRepository repository; // 1. Tiêm thêm Repository
    @GetMapping
    public ResponseEntity<Page<MauSac>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai, // Hứng tham số từ FE
            @RequestParam(required = false) Integer status
    ) {
        Integer filterStatus = (trangThai != null) ? trangThai : status;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.getAll(keyword, filterStatus, pageable));
    }

    // Các hàm create, update, delete, getDetail giữ nguyên như cũ
    @GetMapping("/{id}")
    public ResponseEntity<MauSac> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<MauSac> create(@RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<MauSac> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export() throws IOException {
        ByteArrayInputStream in = service.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh-sach-mau-sac.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
    @GetMapping("/list")
    public ResponseEntity<List<MauSac>> getListForDropdown(
            @RequestParam(required = false) Integer trangThai
    ) {
        if (trangThai != null) {
            return ResponseEntity.ok(repository.findAllByTrangThai(trangThai));
        }
        return ResponseEntity.ok(repository.findAll());
    }
}