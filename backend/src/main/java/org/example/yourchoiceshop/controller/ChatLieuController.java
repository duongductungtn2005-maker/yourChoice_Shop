package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ChatLieu;
import org.example.yourchoiceshop.service.impl.ChatLieuServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
@RestController
@RequestMapping("/api/v1/chat-lieu")
@RequiredArgsConstructor

public class ChatLieuController {
    private final ChatLieuServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<ChatLieu>> getAll(
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
    public ResponseEntity<ChatLieu> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<ChatLieu> create(@RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<ChatLieu> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export() throws IOException {
        ByteArrayInputStream in = service.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh-sach-chat-lieu.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}