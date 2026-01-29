package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.DiaChiRequest;
import org.example.yourchoiceshop.entity.DiaChiKhachHang;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.repository.DiaChiKhachHangRepository;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.service.DiaChiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaChiServiceImpl implements DiaChiService {

    private final DiaChiKhachHangRepository diaChiRepository;
    private final KhachHangRepository khachHangRepository;

    @Override
    public List<DiaChiKhachHang> getAllByKhachHangId(Integer khachHangId) {
        // Chỉ lấy các địa chỉ đang hoạt động (trangThai = 1)
        return diaChiRepository.findByKhachHangIdAndTrangThai(khachHangId, 1);
    }

    @Override
    public DiaChiKhachHang create(DiaChiRequest request) {
        KhachHang kh = khachHangRepository.findById(request.getIdKhachHang())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Logic: Nếu khách chưa có địa chỉ nào -> Cái đầu tiên auto là Mặc định
        List<DiaChiKhachHang> existingList = diaChiRepository.findByKhachHangId(request.getIdKhachHang());
        if (existingList.isEmpty()) {
            request.setMacDinh(true);
        }

        // Logic: Nếu user chọn địa chỉ này là Mặc định -> Bỏ mặc định các cái cũ
        if (Boolean.TRUE.equals(request.getMacDinh())) {
            resetDefaultAddress(request.getIdKhachHang());
        }

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setMaDiaChi("DC" + System.currentTimeMillis()); // Tự sinh mã
        dc.setKhachHang(kh);

        mapRequestToEntity(request, dc);
        dc.setTrangThai(1); // Mặc định hoạt động

        return diaChiRepository.save(dc);
    }

    @Override
    public DiaChiKhachHang update(Integer id, DiaChiRequest request) {
        DiaChiKhachHang dc = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        // Nếu update thành mặc định -> Reset các cái khác
        if (Boolean.TRUE.equals(request.getMacDinh())) {
            resetDefaultAddress(dc.getKhachHang().getId());
        }

        mapRequestToEntity(request, dc);
        return diaChiRepository.save(dc);
    }

    @Override
    public void delete(Integer id) {
        DiaChiKhachHang dc = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        dc.setTrangThai(0); // Xóa mềm
        diaChiRepository.save(dc);
    }

    @Override
    public void setDefault(Integer id) {
        DiaChiKhachHang dc = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        // 1. Reset hết các cái của khách này về false
        resetDefaultAddress(dc.getKhachHang().getId());

        // 2. Set cái này thành true
        dc.setMacDinh(true);
        diaChiRepository.save(dc);
    }

    // --- HÀM PHỤ ---

    // Hàm reset tất cả địa chỉ của khách hàng về "Không mặc định"
    private void resetDefaultAddress(Integer khachHangId) {
        List<DiaChiKhachHang> list = diaChiRepository.findByKhachHangId(khachHangId);
        for (DiaChiKhachHang dc : list) {
            dc.setMacDinh(false);
        }
        diaChiRepository.saveAll(list);
    }

    // Hàm map dữ liệu
    private void mapRequestToEntity(DiaChiRequest req, DiaChiKhachHang dc) {
        dc.setTenNguoiNhan(req.getTenNguoiNhan());
        dc.setSoDienThoai(req.getSoDienThoai());
        dc.setThanhPho(req.getThanhPho());
        dc.setQuan(req.getQuan());
        dc.setPhuong(req.getPhuong());
        dc.setDiaChiCuThe(req.getDiaChiCuThe());
        if (req.getMacDinh() != null) {
            dc.setMacDinh(req.getMacDinh());
        }
    }
}