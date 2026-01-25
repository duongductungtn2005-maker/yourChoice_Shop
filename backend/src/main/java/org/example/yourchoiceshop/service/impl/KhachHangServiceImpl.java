package org.example.yourchoiceshop.service.impl;

import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl {

    private final KhachHangRepository khachHangRepository;

    /**
     * Lấy tất cả khách hàng
     */
    public Page<KhachHang> getAll(Pageable pageable) {
        return khachHangRepository.findAll(pageable);
    }

    /**
     * Lấy khách hàng theo ID
     */
    public KhachHang getById(Integer id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));
    }

    /**
     * Tạo khách hàng mới
     */
    public KhachHang create(KhachHang khachHang) {
        // Tự sinh mã khách hàng nếu không có
        if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
            khachHang.setMaKhachHang("KH" + System.currentTimeMillis());
        }

        // Mặc định trạng thái là 1 (hoạt động)
        if (khachHang.getTrangThai() == null) {
            khachHang.setTrangThai(1);
        }

        return khachHangRepository.save(khachHang);
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public KhachHang update(Integer id, KhachHang khachHangUpdate) {
        KhachHang khachHang = getById(id);

        if (khachHangUpdate.getTenKhachHang() != null) {
            khachHang.setTenKhachHang(khachHangUpdate.getTenKhachHang());
        }
        if (khachHangUpdate.getTenTaiKhoan() != null) {
            khachHang.setTenTaiKhoan(khachHangUpdate.getTenTaiKhoan());
        }
        if (khachHangUpdate.getMatKhau() != null) {
            khachHang.setMatKhau(khachHangUpdate.getMatKhau());
        }
        if (khachHangUpdate.getEmail() != null) {
            khachHang.setEmail(khachHangUpdate.getEmail());
        }
        if (khachHangUpdate.getSoDienThoai() != null) {
            khachHang.setSoDienThoai(khachHangUpdate.getSoDienThoai());
        }
        if (khachHangUpdate.getGioiTinh() != null) {
            khachHang.setGioiTinh(khachHangUpdate.getGioiTinh());
        }
        if (khachHangUpdate.getNgaySinh() != null) {
            khachHang.setNgaySinh(khachHangUpdate.getNgaySinh());
        }
        if (khachHangUpdate.getTrangThai() != null) {
            khachHang.setTrangThai(khachHangUpdate.getTrangThai());
        }

        return khachHangRepository.save(khachHang);
    }

    /**
     * Xóa khách hàng (đánh dấu trạng thái = 0)
     */
    public void delete(Integer id) {
        KhachHang khachHang = getById(id);
        khachHang.setTrangThai(0);
        khachHangRepository.save(khachHang);
    }
}
