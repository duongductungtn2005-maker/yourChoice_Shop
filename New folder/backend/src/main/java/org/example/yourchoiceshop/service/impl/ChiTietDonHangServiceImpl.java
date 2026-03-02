package org.example.yourchoiceshop.service.impl;

import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
import org.example.yourchoiceshop.dto.response.LichSuHoaDonResponse;
import org.example.yourchoiceshop.dto.response.LichSuThanhToanResponse;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.repository.HoaDonChiTietRepository;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuHoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.example.yourchoiceshop.service.ChiTietDonHangService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChiTietDonHangServiceImpl implements ChiTietDonHangService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;

    @Override
    public ChiTietDonHangResponse getChiTietDonHang(String maHoaDon) { // Nhận String

        // 1. Tìm Hóa Đơn theo Mã (String)
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maHoaDon));

        // Lấy ID integer để dùng cho các bảng con
        Integer id = hd.getId();

        ChiTietDonHangResponse res = new ChiTietDonHangResponse();

        // ===== Thông tin chung =====
        res.setId(hd.getId());
        res.setMaHoaDon(hd.getMaHoaDon());
        res.setTenKhachHang(hd.getTenNguoiNhan());
        res.setSoDienThoai(hd.getSdtNguoiNhan());
        res.setDiaChi(hd.getDiaChiNguoiNhan());
        res.setLoaiHoaDon(hd.getLoaiHoaDon());
        res.setTrangThai(hd.getTrangThai());
        res.setNgayTao(hd.getNgayTao());

        res.setTongTien(hd.getTongTien());
        res.setTienGiamGia(hd.getTienGiamGia());
        res.setPhiVanChuyen(hd.getPhiVanChuyen());
        res.setTongTienSauGiam(hd.getTongTienSauGiam());

        // ===== Block UI (Dùng ID integer để tìm) =====
        res.setSanPhamList(
                hoaDonChiTietRepository.findSanPhamByDonHang(id)
        );

        res.setLichSuHoaDon(
                lichSuHoaDonRepository.findByHoaDonIdOrderByThoiGianAsc(id)
                        .stream()
                        .map(LichSuHoaDonResponse::fromEntity)
                        .toList()
        );

        res.setLichSuThanhToan(
                lichSuThanhToanRepository.findByHoaDonId(id)
                        .stream()
                        .map(LichSuThanhToanResponse::fromEntity)
                        .toList()
        );

        return res;
    }
}