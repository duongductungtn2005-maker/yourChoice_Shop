package org.example.yourchoiceshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// import java.util.UUID;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/upload")
@CrossOrigin("*") // Giữ nguyên
public class UploadController {

    // LƯU Ý: Thư mục này sẽ tự tạo ra ngang hàng với file pom.xml
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Tạo thư mục nếu chưa có
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Lưu file vật lý
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            // 3. Trả về URL để Frontend hiển thị
            // QUAN TRỌNG: Phải có chữ "/images/" ở đây để khớp với WebConfig
            String fileUrl = "http://localhost:8080/images/" + fileName;

            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}