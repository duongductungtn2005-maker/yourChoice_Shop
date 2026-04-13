package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.BulkUpdateWrapper;
import org.example.yourchoiceshop.dto.request.CreateProductRequest;
import org.example.yourchoiceshop.dto.request.ProductVariantRequest;
import org.example.yourchoiceshop.dto.request.UpdateVariantRequest;
import org.example.yourchoiceshop.dto.response.ProductResponse;
import org.example.yourchoiceshop.service.impl.ProductServiceImpl;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.yourchoiceshop.dto.response.VariantResponse; 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductServiceImpl productService;

    // 1. API Lấy danh sách sản phẩm (Có lọc nâng cao)
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer idThuongHieu,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(required = false) Integer idXuatXu,
            @RequestParam(required = false) Integer idCoAo,
            @RequestParam(required = false) Integer idTayAo
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(productService.getAllProducts(keyword, status, idThuongHieu, idChatLieu, idXuatXu, idCoAo, idTayAo, pageable));
    }

    // 2. API Tạo mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    // 3. API Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 4. API Lấy danh sách biến thể theo ID cha
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<VariantResponse>> getVariants(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getVariantsByProductId(id));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<VariantResponse> addVariant(@PathVariable Integer id, @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(productService.createVariantForProduct(id, request));
    }

    // 5. API Lấy chi tiết sản phẩm cha
    // Thay đổi kiểu trả về từ SanPham thành ProductResponse
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
    @PutMapping("/variants/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Integer id, @RequestBody UpdateVariantRequest req) {
        productService.updateVariant(id, req);
        return ResponseEntity.ok("Cập nhật thành công");
    }
    // 7. API Cập nhật hàng loạt (Sửa lại để dùng Wrapper)
    @PutMapping("/variants/bulk-update")
    public ResponseEntity<?> bulkUpdateVariants(@RequestBody BulkUpdateWrapper req) {
        // Lấy list từ trong wrapper ra để xử lý
        productService.bulkUpdateVariants(req.getVariants());
        return ResponseEntity.ok("Cập nhật thành công");
    }
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() throws IOException {
        ByteArrayInputStream in = productService.exportProductsToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh-sach-san-pham.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody CreateProductRequest request) {
        // Bạn cần đảm bảo method updateProduct đã tồn tại trong ProductService
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }
}