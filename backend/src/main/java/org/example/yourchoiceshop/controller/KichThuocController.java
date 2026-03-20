package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.KichThuoc;
import org.example.yourchoiceshop.service.impl.KichThuocServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import org.example.yourchoiceshop.repository.KichThuocRepository; // Import Repo
import java.util.List;
@RestController
@RequestMapping("/api/v1/kich-thuoc")
@RequiredArgsConstructor
public class KichThuocController {
    private final KichThuocServiceImpl service;
    private final KichThuocRepository repository; // 1. Tiêm thêm Repository
    @GetMapping
    public ResponseEntity<Page<KichThuoc>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer status
    ) {
        Integer filterStatus = (trangThai != null) ? trangThai : status;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.getAll(keyword, filterStatus, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KichThuoc> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<KichThuoc> create(@RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<KichThuoc> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export() throws IOException {
        ByteArrayInputStream in = service.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh-sach-kich-thuoc.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
    @GetMapping("/list")
    public ResponseEntity<List<KichThuoc>> getListForDropdown(
            @RequestParam(required = false) Integer trangThai
    ) {
        if (trangThai != null) {
            return ResponseEntity.ok(repository.findAllByTrangThai(trangThai));
        }
        return ResponseEntity.ok(repository.findAll());
    }
}