package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;

    // ====== CŨ – GIỮ NGUYÊN ======
    public Page<QuanLyDonHangResponse> searchDonHang(
            QuanLyDonHangRequest request) {

        Pageable pageable = PageRequest.of(
            request.getPage(),
            request.getSize()
        );

        return hoaDonRepository.searchHoaDon(
            request.getKeyword(),
            request.getLoaiHoaDon(),
            request.getTrangThai(),
            pageable
        );
    }

    // ====== MỚI – CHO CHI TIẾT ĐƠN HÀNG ======
    public ChiTietDonHangResponse getChiTietDonHang(Long id) {
    HoaDon hd = hoaDonRepository.findById(id)
        .orElseThrow(() ->
            new RuntimeException("Không tìm thấy hóa đơn"));

    ChiTietDonHangResponse dto = new ChiTietDonHangResponse();

    dto.setId(hd.getId());
    dto.setMaHoaDon(hd.getMaHoaDon());
    dto.setLoaiHoaDon(hd.getLoaiHoaDon().name());
    dto.setTrangThai(hd.getTrangThai());
    dto.setNgayTao(hd.getNgayTao());

    dto.setTongTienSauGiam(hd.getTongTienSauGiam());
    dto.setTienGiamGia(hd.getTienGiamGia());
    dto.setPhiVanChuyen(hd.getPhiVanChuyen());

    // Khách hàng
    if (hd.getKhachHang() != null) {
        dto.setTenKhachHang(hd.getKhachHang().getTenKhachHang());
    }

    // TODO: map tiếp
    // dto.setSanPhamList(...)
    // dto.setLichSuThanhToan(...)
    // dto.setThongTinNhanHang(...)

    return dto;
}

}
