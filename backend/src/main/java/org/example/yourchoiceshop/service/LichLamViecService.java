package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.dto.request.LichLamViecRequest;

// THÊM CÁC DÒNG IMPORT REPOSITORY NÀY
import org.example.yourchoiceshop.repository.LichLamViecRepository; 
import org.example.yourchoiceshop.repository.CaLamViecRepository; 
import org.example.yourchoiceshop.repository.NhanVienRepository; 

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LichLamViecService {

    private final LichLamViecRepository lichLamViecRepository;
    private final CaLamViecRepository caLamViecRepository; 
    private final NhanVienRepository nhanVienRepository;   

    public List<LichLamViec> getLichLamViec(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return lichLamViecRepository.findByNgayLamViecBetweenOrderByNgayLamViecAsc(startDate, endDate);
        }
        return lichLamViecRepository.findAllByOrderByNgayLamViecAsc();
    }

    @Transactional // Thêm cái này để quản lý giao dịch an toàn
    public LichLamViec create(LichLamViecRequest request) {
        CaLamViec ca = caLamViecRepository.findById(request.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ca làm việc"));
        
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân viên"));

        LichLamViec lich = new LichLamViec();
        lich.setCaLamViec(ca);
        lich.setNhanVien(nv);
        lich.setNgayLamViec(request.getNgayLamViec());
        
        // NẾU BaseStatusEntity CỦA BẠN CÓ TRƯỜNG TRẠNG THÁI, HÃY GÁN MẶC ĐỊNH Ở ĐÂY:
        // lich.setStatus(1);  HOẶC lich.setTrangThai(1); 

        return lichLamViecRepository.save(lich);
    }
    // 4. Cập nhật lịch làm việc (Sửa)
    public LichLamViec update(Integer id, LichLamViecRequest request) {
        // Tìm lịch cũ
        LichLamViec lich = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch làm việc"));
        
        // Tìm Ca và Nhân viên mới
        CaLamViec ca = caLamViecRepository.findById(request.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ca làm việc"));
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân viên"));

        // Cập nhật thông tin
        lich.setCaLamViec(ca);
        lich.setNhanVien(nv);
        lich.setNgayLamViec(request.getNgayLamViec());

        return lichLamViecRepository.save(lich);
    }
    public void delete(Integer id) {
        if (!lichLamViecRepository.existsById(id)) {
            throw new RuntimeException("Lịch làm việc không tồn tại");
        }
        lichLamViecRepository.deleteById(id);
    }
}