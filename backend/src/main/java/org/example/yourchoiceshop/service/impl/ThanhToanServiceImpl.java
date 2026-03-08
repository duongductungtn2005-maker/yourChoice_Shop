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

    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final HoaDonRepository hoaDonRepo;

    @Override
    @Transactional
    public void thanhToan(String maHoaDon, ThanhToanRequest req) {

        HoaDon hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // ❌ Chỉ cho thanh toán khi CHỜ THANH TOÁN
        if (hoaDon.getTrangThai() != 4) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán");
        }

        // 1️⃣ LƯU LỊCH SỬ THANH TOÁN
        LichSuThanhToan ls = new LichSuThanhToan();
        ls.setHoaDon(hoaDon);
        ls.setSoTien(req.getSoTien());
        ls.setHinhThucThanhToan(req.getHinhThucThanhToan());
        ls.setNgayThanhToan(LocalDateTime.now());
        ls.setGhiChu(req.getGhiChu());
        ls.setTrangThai(1);

        lichSuThanhToanRepository.save(ls);

        // 2️⃣ HOÀN THÀNH ĐƠN
        hoaDon.setTongTienSauGiam(req.getSoTien());
        hoaDon.setTrangThai(5);
        hoaDonRepo.save(hoaDon);
    }

    @Transactional
    public void thanhToanHoaDon(String maHoaDon, ThanhToanRequest req) {

        HoaDon hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // ❌ Chỉ cho thanh toán khi chờ thanh toán
        if (hoaDon.getTrangThai() != 4) {
            throw new RuntimeException("Hóa đơn không ở trạng thái chờ thanh toán");
        }

        // 1️⃣ Lưu lịch sử thanh toán
        LichSuThanhToan ls = new LichSuThanhToan();
        ls.setHoaDon(hoaDon);
        ls.setSoTien(hoaDon.getTongTienSauGiam());
        ls.setHinhThucThanhToan(req.getHinhThucThanhToan());
        ls.setNgayThanhToan(LocalDateTime.now());
        ls.setTrangThai(1);

        lichSuThanhToanRepository.save(ls);

        // 2️⃣ Chuyển trạng thái → HOÀN THÀNH
        hoaDon.setTrangThai(5);
        hoaDonRepo.save(hoaDon);
    }
}