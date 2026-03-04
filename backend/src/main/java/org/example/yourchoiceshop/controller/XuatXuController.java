package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.XuatXu;
import org.example.yourchoiceshop.service.impl.XuatXuServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/xuat-xu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class XuatXuController {

    private final XuatXuServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<XuatXu>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            // Backend hứng tham số 'status' (Frontend gửi status=1)
            @RequestParam(required = false) Integer status
    ) {
        // Nếu gọi từ Dropdown (size=100), ta sort theo tên (hoặc ID) để dễ tìm
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Xử lý keyword null để tránh lỗi (nếu Service không xử lý)
        String searchKey = (keyword != null) ? keyword : "";

        return ResponseEntity.ok(service.getAll(searchKey, status, pageable));
    }

    // ... (Giữ nguyên các hàm bên dưới của bạn: getDetail, create, update, delete) ...
    @GetMapping("/{id}")
    public ResponseEntity<XuatXu> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<XuatXu> create(@RequestBody StoreAttributeRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<XuatXu> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export() throws IOException {
        ByteArrayInputStream in = service.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh-sach-xuat-xu.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}