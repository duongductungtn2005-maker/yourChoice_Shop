package org.example.yourchoiceshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.TayAo;
import org.example.yourchoiceshop.service.impl.TayAoServiceImpl;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tay-ao")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TayAoController {
    private final TayAoServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<TayAo>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(PageRequest.of(page, size, Sort.by("id").descending())));
    }
    @GetMapping("/{id}")
    public ResponseEntity<TayAo> getDetail(@PathVariable Integer id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping
    public ResponseEntity<TayAo> create(@RequestBody @Valid StoreAttributeRequest req) { return ResponseEntity.ok(service.create(req)); }
    @PutMapping("/{id}")
    public ResponseEntity<TayAo> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest req) { return ResponseEntity.ok(service.update(id, req)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}