package org.example.yourchoiceshop.controller;

import lombok.Getter;
import lombok.Setter;
import org.example.yourchoiceshop.entity.*;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/chi-tiet-san-pham")

public class ChiTietSanPhamController {

    @Autowired
    private ChiTietSanPhamRepository repository;

    // API Lấy danh sách
    @GetMapping
    public ResponseEntity<Page<ChiTietSanPham>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            // Thêm các tham số lọc vào đây
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer idMauSac,
            @RequestParam(required = false) Integer idKichThuoc,
            @RequestParam(required = false) Integer trangThai
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // Gọi hàm search trong Repository thay vì findAll
        Page<ChiTietSanPham> result = repository.searchByCriteria(
                keyword,
                idMauSac,
                idKichThuoc,
                trangThai,
                pageable
        );

        return ResponseEntity.ok(result);
    }

    // API Tìm biến thể theo mã CTSP (dùng cho quét QR)
    @GetMapping("/by-ma")
    public ResponseEntity<?> getByMaCtsp(@RequestParam String maCtsp) {
        return repository.findByMaCtspAndActive(maCtsp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- API SỬA (FIX LỖI TẠI ĐÂY) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody UpdateChiTietRequest request) {
        Optional<ChiTietSanPham> optional = repository.findById(id);

        if (optional.isPresent()) {
            ChiTietSanPham detail = optional.get();

            // 1. Cập nhật thông tin cơ bản
            detail.setGiaBan(request.getGiaBan());
            detail.setSoLuong(request.getSoLuong());
            detail.setTrangThai(request.getTrangThai());

            // 2. Map các ID khóa ngoại sang Object
            // Màu sắc
            if (request.getIdMauSac() != null) {
                MauSac ms = new MauSac();
                ms.setId(request.getIdMauSac());
                detail.setMauSac(ms);
            }
            // Kích thước
            if (request.getIdKichThuoc() != null) {
                KichThuoc kt = new KichThuoc();
                kt.setId(request.getIdKichThuoc());
                detail.setKichThuoc(kt);
            }
            // Chất liệu
            if (request.getIdChatLieu() != null) {
                ChatLieu cl = new ChatLieu();
                cl.setId(request.getIdChatLieu());
                detail.setChatLieu(cl);
            }
            // Thương hiệu
            if (request.getIdThuongHieu() != null) {
                ThuongHieu th = new ThuongHieu();
                th.setId(request.getIdThuongHieu());
                detail.setThuongHieu(th);
            }
            // Cổ áo
            if (request.getIdCoAo() != null) {
                CoAo ca = new CoAo();
                ca.setId(request.getIdCoAo());
                detail.setCoAo(ca);
            }
            // Tay áo
            if (request.getIdTayAo() != null) {
                TayAo ta = new TayAo();
                ta.setId(request.getIdTayAo());
                detail.setTayAo(ta);
            }
            // Xuất xứ
            if (request.getIdXuatXu() != null) {
                XuatXu xx = new XuatXu();
                xx.setId(request.getIdXuatXu());
                detail.setXuatXu(xx);
            }

            repository.save(detail);
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reserve")
    @Transactional
    public ResponseEntity<?> reserveStock(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        Integer soLuong = request.get("soLuong");
        if (soLuong == null || soLuong <= 0) {
            return ResponseEntity.badRequest().body("Số lượng không hợp lệ");
        }
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        int updated = repository.reserveStock(id, soLuong);
        if (updated == 0) {
            return ResponseEntity.badRequest().body("Không đủ tồn kho");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    @Transactional
    public ResponseEntity<?> releaseStock(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        Integer soLuong = request.get("soLuong");
        if (soLuong == null || soLuong <= 0) {
            return ResponseEntity.badRequest().body("Số lượng không hợp lệ");
        }
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.releaseStock(id, soLuong);
        return ResponseEntity.ok().build();
    }

    // --- DTO: Class phụ để hứng dữ liệu JSON từ Frontend ---
    @Getter
    @Setter
    public static class UpdateChiTietRequest {
        private BigDecimal giaBan;
        private Integer soLuong;
        private Integer trangThai;

        // Các trường ID mà Frontend gửi lên
        private Integer idMauSac;
        private Integer idKichThuoc;
        private Integer idChatLieu;
        private Integer idThuongHieu;
        private Integer idCoAo;
        private Integer idTayAo;
        private Integer idXuatXu;
    }
}