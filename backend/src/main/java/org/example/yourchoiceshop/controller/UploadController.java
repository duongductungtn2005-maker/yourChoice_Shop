package org.example.yourchoiceshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    // Thư mục lưu file upload (tạo tự động nếu chưa có)
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File trống!");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh!");
            }

            // 1. Tạo thư mục nếu chưa có
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Tạo tên file an toàn (loại bỏ ký tự đặc biệt, tiếng Việt có dấu)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            
            // Lấy phần mở rộng file
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Tạo tên file mới: timestamp + UUID + extension (đảm bảo unique và an toàn)
            String safeFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // 3. Lưu file vật lý
            Path filePath = Paths.get(UPLOAD_DIR + safeFileName);
            Files.write(filePath, file.getBytes());

            // 4. Trả về CHỈ TÊN FILE (không phải full URL)
            return ResponseEntity.ok(Map.of("url", safeFileName));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi lưu file: " + e.getMessage());
        }
    }
}