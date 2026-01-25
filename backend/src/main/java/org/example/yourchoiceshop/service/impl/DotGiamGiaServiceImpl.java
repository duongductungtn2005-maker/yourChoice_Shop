package org.example.yourchoiceshop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.DotGiamGia;
import org.example.yourchoiceshop.repository.ChiTietDotGiamGiaRepository;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.DotGiamGiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaServiceImpl {
    private final DotGiamGiaRepository dotRepo;
    private final ChiTietDotGiamGiaRepository ctDotRepo;
    private final ChiTietSanPhamRepository ctspRepo;

    public Page<DotGiamGia> getAll(String keyword, Pageable pageable) {
        return dotRepo.search(keyword, pageable);
    }

    public DotGiamGia getById(Integer id) {
        return dotRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá: " + id));
    }

    @Transactional
    public DotGiamGia create(DotGiamGiaRequest req) {
        // 1. Lưu đợt giảm giá (Bảng cha)
        DotGiamGia dot = new DotGiamGia();
        dot.setMaDotGiamGia(req.getMaDotGiamGia() != null ? req.getMaDotGiamGia() : "EVENT" + System.currentTimeMillis());
        mapReqToEntity(req, dot);
        DotGiamGia savedDot = dotRepo.save(dot);

        // 2. Lưu chi tiết các sản phẩm được áp dụng (Bảng con)
        if (req.getIdChiTietSanPhams() != null && !req.getIdChiTietSanPhams().isEmpty()) {
            List<ChiTietDotGiamGia> listCT = new ArrayList<>();
            for (Integer idCtsp : req.getIdChiTietSanPhams()) {
                // Kiểm tra sản phẩm có tồn tại không
                ChiTietSanPham ctsp = ctspRepo.findById(idCtsp).orElse(null);
                if (ctsp != null) {
                    ChiTietDotGiamGia ctdgg = new ChiTietDotGiamGia();
                    ctdgg.setDotGiamGia(savedDot);
                    ctdgg.setChiTietSanPham(ctsp);
                    ctdgg.setTrangThai(1);
                    // Có thể set thêm giá trị giảm riêng nếu cần logic phức tạp hơn
                    listCT.add(ctdgg);
                }
            }
            ctDotRepo.saveAll(listCT);
        }
        return savedDot;
    }

    @Transactional
    public DotGiamGia update(Integer id, DotGiamGiaRequest req) {
        DotGiamGia dot = getById(id);
        mapReqToEntity(req, dot);

        // Logic cập nhật sản phẩm áp dụng:
        // Cách đơn giản nhất: Xóa hết chi tiết cũ -> Thêm mới lại
        List<ChiTietDotGiamGia> oldDetails = ctDotRepo.findByDotGiamGiaId(id);
        ctDotRepo.deleteAll(oldDetails);

        if (req.getIdChiTietSanPhams() != null) {
            List<ChiTietDotGiamGia> newDetails = new ArrayList<>();
            for (Integer idCtsp : req.getIdChiTietSanPhams()) {
                ChiTietSanPham ctsp = ctspRepo.findById(idCtsp).orElse(null);
                if (ctsp != null) {
                    ChiTietDotGiamGia ctdgg = new ChiTietDotGiamGia();
                    ctdgg.setDotGiamGia(dot);
                    ctdgg.setChiTietSanPham(ctsp);
                    ctdgg.setTrangThai(1);
                    newDetails.add(ctdgg);
                }
            }
            ctDotRepo.saveAll(newDetails);
        }
        return dotRepo.save(dot);
    }

    public void delete(Integer id) {
        DotGiamGia dot = getById(id);
        dot.setTrangThai(0);
        dotRepo.save(dot);

        // Xóa mềm luôn các chi tiết
        List<ChiTietDotGiamGia> details = ctDotRepo.findByDotGiamGiaId(id);
        details.forEach(d -> {
            d.setTrangThai(0);
            ctDotRepo.save(d);
        });
    }

    private void mapReqToEntity(DotGiamGiaRequest req, DotGiamGia entity) {
        entity.setTenDotGiamGia(req.getTenDotGiamGia());
        entity.setGiaTriGiam(req.getGiaTriGiam());
        entity.setLoaiGiamGia(req.getLoaiGiamGia());
        entity.setNgayBatDau(req.getNgayBatDau());
        entity.setNgayKetThuc(req.getNgayKetThuc());
        entity.setTrangThai(req.getTrangThai());
    }
}