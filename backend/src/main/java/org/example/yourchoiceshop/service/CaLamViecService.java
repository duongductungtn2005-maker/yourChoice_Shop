package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.CaLamViecRequest;
import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.repository.CaLamViecRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaLamViecService {

    private final CaLamViecRepository caLamViecRepository;

    // Lấy danh sách tất cả ca làm việc
    public List<CaLamViec> getAll() {
        return caLamViecRepository.findAll();
    }

    // Thêm mới ca làm việc
    public CaLamViec create(CaLamViecRequest request) {
        CaLamViec ca = new CaLamViec();
        
        ca.setMaCa(request.getMaCa());
        ca.setTenCa(request.getTenCa());
        ca.setThoiGianBatDau(request.getThoiGianBatDau());
        ca.setThoiGianKetThuc(request.getThoiGianKetThuc());
        ca.setGhiChu(request.getGhiChu());
        
        // Mặc định trạng thái là 1 (Đang hoạt động) nếu không truyền vào
        ca.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : 1);
        
        return caLamViecRepository.save(ca);
    }
    public Page<CaLamViec> searchAndFilter(String keyword, Integer status, LocalTime startTime, LocalTime endTime, Pageable pageable) {
    // Ép LocalTime sang String dạng HH:mm
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String startTimeStr = (startTime != null) ? startTime.format(formatter) : null;
    String endTimeStr = (endTime != null) ? endTime.format(formatter) : null;

    // Truyền chuỗi xuống Repository
    return caLamViecRepository.searchAndFilter(keyword, status, startTimeStr, endTimeStr, pageable);
}

    // Cập nhật trạng thái của ca làm việc
    public CaLamViec updateTrangThai(Integer id, Integer trangThai) {
        CaLamViec ca = caLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CaLamViec not found with id: " + id));
        ca.setTrangThai(trangThai);
        return caLamViecRepository.save(ca);
    }
}