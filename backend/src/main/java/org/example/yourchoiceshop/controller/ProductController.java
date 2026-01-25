package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.CreateProductRequest;
import org.example.yourchoiceshop.dto.response.ProductResponse;
import org.example.yourchoiceshop.entity.SanPham;
import org.example.yourchoiceshop.service.impl.ProductServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Cho phép VueJS gọi API
public class ProductController {

    private final ProductServiceImpl productService;

    // GET: Lấy danh sách sản phẩm (có phân trang)
    // URL: /api/v1/products?page=0&size=10&keyword=ao
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        return ResponseEntity.ok(productService.getAllProducts(keyword, pageable));
    }

    // GET: Lấy chi tiết 1 sản phẩm (để xem hoặc sửa)
    @GetMapping("/{id}")
    public ResponseEntity<SanPham> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // POST: Thêm mới sản phẩm + biến thể
    @PostMapping
    public ResponseEntity<SanPham> create(@RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    // DELETE: Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}