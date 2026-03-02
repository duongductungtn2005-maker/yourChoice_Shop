// package org.example.yourchoiceshop.controller;

// import org.example.yourchoiceshop.dto.request.TaoGiaoCaRequest;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;

// public class GiaoCaController {
//     // Thêm API này vào GiaoCaController
// @PostMapping("/them-nhanh")
// public ResponseEntity<?> themGiaoCaNhanh(@RequestBody TaoGiaoCaRequest request) {
//     try {
//         giaoCaService.taoGiaoCaNhanh(request);
//         return ResponseEntity.ok().body("Thêm giao ca thành công!");
//     } catch (Exception e) {
//         return ResponseEntity.badRequest().body("Lỗi khi thêm: " + e.getMessage());
//     }
// }
// }
