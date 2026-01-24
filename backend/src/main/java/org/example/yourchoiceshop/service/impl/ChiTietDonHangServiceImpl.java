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

public class ChiTietDonHangServiceImpl implements ChiTietDonHangService {
    
    private HoaDonRepository hoaDonRepository;
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Override
    public ChiTietDonHangResponse getChiTietDonHang(Integer id) {

        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy đơn hàng"));

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

        // ===== Block UI =====
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
