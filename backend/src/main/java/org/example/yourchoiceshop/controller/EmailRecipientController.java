package org.example.yourchoiceshop.controller;

import org.example.yourchoiceshop.entity.EmailRecipient;
import org.example.yourchoiceshop.repository.EmailRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-recipients")
// @CrossOrigin("*") // Mở comment dòng này nếu mày bị lỗi CORS ở Frontend nhé
public class EmailRecipientController {

    @Autowired
    private EmailRecipientRepository repository;

    // 1. Lấy toàn bộ danh sách danh bạ
    @GetMapping
    public ResponseEntity<List<EmailRecipient>> getAll() {
        // Lấy danh sách và sắp xếp id giảm dần (mới nhất lên đầu)
        return ResponseEntity.ok(repository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")));
    }

    // 2. Thêm mới một email vào danh bạ
    @PostMapping
    public ResponseEntity<?> addRecipient(@RequestBody EmailRecipient request) {
        if (repository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email này đã tồn tại trong hệ thống!");
        }
        EmailRecipient saved = repository.save(request);
        return ResponseEntity.ok(saved);
    }

    // 3. Xóa email khỏi danh bạ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipient(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.badRequest().body("Không tìm thấy email này!");
        }
        repository.deleteById(id);
        return ResponseEntity.ok("Đã xóa thành công!");
    }
}