package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.response.ProductDetailResponse;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chi-tiet-san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChiTietSanPhamController {

    private final ChiTietSanPhamRepository ctspRepo;

    // API lấy tất cả sản phẩm chi tiết đang hoạt động
    @GetMapping("/active")
    public ResponseEntity<List<ProductDetailResponse>> getAllActive() {
        // 1. Lấy tất cả từ DB
        List<ChiTietSanPham> listEntities = ctspRepo.findAll();
        List<ProductDetailResponse> listResponses = new ArrayList<>();

        // 2. Convert sang DTO
        for (ChiTietSanPham ctsp : listEntities) {
            // Chỉ lấy sản phẩm có trạng thái = 1 (Hoạt động)
            if (ctsp.getTrangThai() != null && ctsp.getTrangThai() == 1) {
                ProductDetailResponse res = new ProductDetailResponse();
                res.setId(ctsp.getId());
                res.setMaSanPham(ctsp.getMaCtsp());
                res.setSoLuong(ctsp.getSoLuong());
                res.setGiaBan(ctsp.getGiaBan());

                // Xử lý null an toàn (tránh lỗi nếu data thiếu)
                res.setTenSanPham(ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Unknown");
                res.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "-");
                res.setKichThuoc(ctsp.getKichThuoc() != null ? ctsp.getKichThuoc().getTenKichThuoc() : "-");

                listResponses.add(res);
            }
        }
        return ResponseEntity.ok(listResponses);
    }

    @GetMapping("/by-product/{id}")
    public ResponseEntity<?> getByProductId(@PathVariable Integer id) {
        List<ChiTietSanPham> list = ctspRepo.findAllBySanPhamId(id);
        List<ProductDetailResponse> responses = new ArrayList<>();

        for (ChiTietSanPham ctsp : list) {
            ProductDetailResponse res = new ProductDetailResponse();
            res.setId(ctsp.getId());
            res.setMaSanPham(ctsp.getMaCtsp());
            res.setTenSanPham(ctsp.getSanPham().getTenSanPham()); // Tên SP cha
            res.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "-");
            res.setKichThuoc(ctsp.getKichThuoc() != null ? ctsp.getKichThuoc().getTenKichThuoc() : "-");
            res.setGiaBan(ctsp.getGiaBan());
            res.setSoLuong(ctsp.getSoLuong());
            responses.add(res);
        }
        return ResponseEntity.ok(responses);
    }

}