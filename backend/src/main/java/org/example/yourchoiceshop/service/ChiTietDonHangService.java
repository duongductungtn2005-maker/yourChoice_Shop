package org.example.yourchoiceshop.service;

import java.util.List;

import org.example.yourchoiceshop.dto.response.*;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ChiTietDonHangService {

    private final HoaDonRepository hoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;

    public ChiTietDonHangResponse getChiTietDonHang(Long idHoaDon) {

    HoaDon hd = hoaDonRepository.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

    ChiTietDonHangResponse res = new ChiTietDonHangResponse();

    // ===== THÔNG TIN CHUNG =====
    res.setId(hd.getId());
    res.setMaHoaDon(hd.getMaHoaDon());
    res.setLoaiHoaDon(hd.getLoaiHoaDon().name());
    res.setTrangThai(hd.getTrangThai());
    res.setNgayTao(hd.getNgayTao());

    // ===== TIỀN =====
    res.setTongTienSauGiam(hd.getTongTienSauGiam());
    res.setTienGiamGia(hd.getTienGiamGia());
    res.setPhiVanChuyen(hd.getPhiVanChuyen());

    // ===== KHÁCH HÀNG =====
if (hd.getKhachHang() != null) {
    res.setTenKhachHang(
        hd.getKhachHang().getTenKhachHang()
    );
}


    // ===== DANH SÁCH SẢN PHẨM =====
List<SanPhamTrongDonResponse> spList = hd.getChiTietHoaDons()
    .stream()
    .map(ct -> new SanPhamTrongDonResponse(
            ct.getChiTietSanPham()
              .getSanPham()
              .getTenSanPham(),

            ct.getChiTietSanPham()
              .getKichThuoc()
              .getTenKichThuoc(),

            ct.getChiTietSanPham()
              .getMauSac()
              .getTenMauSac(),

            ct.getSoLuong(),
            ct.getThanhTien()
    ))
    .toList();


res.setSanPhamList(spList);


    // ===== LỊCH SỬ THANH TOÁN =====
    List<LichSuThanhToanResponse> ls = lichSuThanhToanRepository
            .findByHoaDonId(hd.getId())
            .stream()
            .map(t -> new LichSuThanhToanResponse(
                    t.getSoTien(),
                    t.getNgayThanhToan(),
                    t.getHinhThucThanhToan()
            ))
            .toList();

    res.setLichSuThanhToan(ls);

    // ===== THÔNG TIN NHẬN HÀNG =====
    if (hd.getLoaiHoaDon() == LoaiHoaDon.TRUC_TUYEN) {
        res.setThongTinNhanHang(
                new ThongTinNhanHangResponse(
                        hd.getTenNguoiNhan(),
                        hd.getSdtNguoiNhan(),
                        hd.getDiaChiNguoiNhan()
                )
        );
    }

    return res;
}
}
