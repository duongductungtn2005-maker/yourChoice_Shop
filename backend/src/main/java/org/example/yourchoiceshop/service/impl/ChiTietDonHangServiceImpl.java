// package org.example.yourchoiceshop.service.impl;

// import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
// import org.example.yourchoiceshop.dto.response.LichSuHoaDonResponse;
// import org.example.yourchoiceshop.dto.response.LichSuThanhToanResponse;
// import org.example.yourchoiceshop.dto.response.NhanVienDonHangResponse;
// import org.example.yourchoiceshop.entity.HoaDon;
// import org.example.yourchoiceshop.entity.NhanVien;
// import org.example.yourchoiceshop.repository.ChiTietDonHangRepository;
// import org.example.yourchoiceshop.repository.HoaDonRepository;
// import org.example.yourchoiceshop.repository.LichSuHoaDonRepository;
// import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
// import org.example.yourchoiceshop.service.ChiTietDonHangService;
// import org.springframework.stereotype.Service;

// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class ChiTietDonHangServiceImpl implements ChiTietDonHangService {

//         private final HoaDonRepository hoaDonRepository;
//         private final ChiTietDonHangRepository chiTietDonHangRepository;
//         private final LichSuHoaDonRepository lichSuHoaDonRepository;
//         private final LichSuThanhToanRepository lichSuThanhToanRepository;

//         @Override
//         public ChiTietDonHangResponse getChiTietDonHang(Integer id) {

//                 HoaDon hd = hoaDonRepository.findById(id)
//                                 .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng id=" + id));


//                 ChiTietDonHangResponse res = new ChiTietDonHangResponse();

//                 // ===== Thông tin chung =====
//                 res.setId(hd.getId());
//                 res.setMaHoaDon(hd.getMaHoaDon());
//                 res.setTenKhachHang(hd.getTenNguoiNhan());
//                 res.setSoDienThoai(hd.getSdtNguoiNhan());
//                 res.setDiaChi(hd.getDiaChiNguoiNhan());
//                 res.setLoaiHoaDon(hd.getLoaiHoaDon());
//                 res.setTrangThai(hd.getTrangThai());
//                 res.setNgayTao(hd.getNgayTao());

//                 res.setTongTien(hd.getTongTien());
//                 res.setTienGiamGia(hd.getTienGiamGia());
//                 res.setPhiVanChuyen(hd.getPhiVanChuyen());
//                 res.setTongTienSauGiam(hd.getTongTienSauGiam());

//                 // ===== NHÂN VIÊN XỬ LÝ =====
//                 if (hd.getNhanVien() != null) {
//                         NhanVien nv = hd.getNhanVien();
//                         res.setNhanVien(
//                                         NhanVienDonHangResponse.builder()
//                                                         .id(nv.getId())
//                                                         .maNhanVien(nv.getMaNhanVien())
//                                                         .tenNhanVien(nv.getTenNhanVien())
//                                                         .soDienThoai(nv.getSoDienThoai())
//                                                         .email(nv.getEmail())
//                                                         .build());
//                 }

//                 // ===== Block UI =====
//                 res.setSanPhamList(
//                                 chiTietDonHangRepository.findSanPhamByDonHang(id));

//                 res.setLichSuHoaDon(
//                                 lichSuHoaDonRepository.findByHoaDonIdOrderByThoiGianAsc(id)
//                                                 .stream()
//                                                 .map(LichSuHoaDonResponse::fromEntity)
//                                                 .toList());

//                 res.setLichSuThanhToan(
//                                 lichSuThanhToanRepository.findByHoaDonId(id)
//                                                 .stream()
//                                                 .map(LichSuThanhToanResponse::fromEntity)
//                                                 .toList());

//                 return res;
//         }

// }
