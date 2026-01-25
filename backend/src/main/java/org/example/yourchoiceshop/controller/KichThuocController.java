package org.example.yourchoiceshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.KichThuoc;
import org.example.yourchoiceshop.service.impl.KichThuocServiceImpl;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kich-thuoc")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KichThuocController {
    private final KichThuocServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<KichThuoc>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size, Sort.by("id").descending())));
    }
    @GetMapping("/{id}")
    public ResponseEntity<KichThuoc> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<KichThuoc> create(@RequestBody @Valid StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<KichThuoc> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}