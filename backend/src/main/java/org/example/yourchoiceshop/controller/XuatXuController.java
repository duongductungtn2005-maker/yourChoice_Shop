package org.example.yourchoiceshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.XuatXu;
import org.example.yourchoiceshop.service.impl.XuatXuServiceImpl;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/xuat-xu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class XuatXuController {
    private final XuatXuServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<XuatXu>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size, Sort.by("id").descending())));
    }
    @GetMapping("/{id}")
    public ResponseEntity<XuatXu> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<XuatXu> create(@RequestBody @Valid StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<XuatXu> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}