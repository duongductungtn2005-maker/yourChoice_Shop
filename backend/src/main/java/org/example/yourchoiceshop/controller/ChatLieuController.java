package org.example.yourchoiceshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ChatLieu;
import org.example.yourchoiceshop.service.impl.ChatLieuServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-lieu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatLieuController {

    private final ChatLieuServiceImpl chatLieuService;

    @GetMapping
    public ResponseEntity<Page<ChatLieu>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(chatLieuService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatLieu> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(chatLieuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ChatLieu> create(@RequestBody @Valid StoreAttributeRequest request) {
        return ResponseEntity.ok(chatLieuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatLieu> update(@PathVariable Integer id, @RequestBody StoreAttributeRequest request) {
        return ResponseEntity.ok(chatLieuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        chatLieuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}