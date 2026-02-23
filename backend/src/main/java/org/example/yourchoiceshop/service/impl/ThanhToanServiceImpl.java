package org.example.yourchoiceshop.service.impl;

import java.time.LocalDateTime;

import org.example.yourchoiceshop.dto.request.ThanhToanRequest;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.example.yourchoiceshop.service.ThanhToanService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThanhToanServiceImpl implements ThanhToanService {

    private final HoaDonRepository hoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;

    @Transactional
    @Override
    public void thanhToan(String maHoaDon, ThanhToanRequest req) {

        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // 1️⃣ Lưu lịch sử thanh toán
        LichSuThanhToan ls = new LichSuThanhToan();
        ls.setHoaDon(hoaDon);
        ls.setSoTien(req.getSoTien());
        ls.setHinhThucThanhToan(req.getHinhThucThanhToan());
        ls.setNgayThanhToan(LocalDateTime.now());
        ls.setGhiChu(req.getGhiChu());
        ls.setTrangThai(1);
        lichSuThanhToanRepository.save(ls);

        // 2️⃣ ✅ CHỐT TIỀN – KHÔNG ĐỤNG GIẢM GIÁ
        hoaDon.setTongTienSauGiam(req.getSoTien());

        // ❌ XÓA 2 DÒNG NÀY
        // hoaDon.setTongTien(req.getSoTien());
        // hoaDon.setTienGiamGia(BigDecimal.ZERO);

        // 3️⃣ Hoàn thành
        hoaDon.setTrangThai(5);
        hoaDonRepository.save(hoaDon);
    }
}