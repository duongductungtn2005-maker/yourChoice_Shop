package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.repository.GiaoCaRepository;
import org.example.yourchoiceshop.repository.LichLamViecRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GiaoCaService {
    
    private final GiaoCaRepository repo;
    private final LichLamViecRepository lichLamViecRepo;
    private final NhanVienRepository nhanVienRepo;

    // ==========================================
    // 1. LẤY LỊCH SỬ GIAO CA (Dùng cho Admin)
    // ==========================================
    public Page<LichSuHoatDongDTO> getLichSu(String search, String tenCa, 
                                             LocalDate startDate, LocalDate endDate, 
                                             Pageable pageable) {
        return repo.findLichSuHoatDong(search, tenCa, startDate, endDate, pageable);
    }

    /**
     * Lấy ca làm việc ĐANG MỞ
     */
    public Optional<GiaoCa> getCurrentActiveShift(Integer idNhanVien) {
        // Trạng thái 1 = Đang làm
        return repo.findByNhanVienTrongCa_IdAndTrangThai(idNhanVien, 1); 
    }

    /**
     * MỞ CA LÀM VIỆC
     */
    public GiaoCa openShift(Integer idNhanVien, Integer idLichLamViec) {
        // 1. Kiểm tra ca đang mở
        if (getCurrentActiveShift(idNhanVien).isPresent()) {
            throw new RuntimeException("Bạn đang có một ca làm việc chưa kết thúc!");
        }

        // 2. Lấy thông tin
        LichLamViec lichLamViec = lichLamViecRepo.findById(idLichLamViec)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin lịch làm việc!"));
            
        NhanVien nhanVien = nhanVienRepo.findById(idNhanVien)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên!"));

        // 3. Khởi tạo Giao Ca
        GiaoCa newShift = new GiaoCa();
        newShift.setMaGiaoCa("GC_" + System.currentTimeMillis()); 
        newShift.setNhanVienTrongCa(nhanVien);
        newShift.setLichLamViec(lichLamViec);
        newShift.setThoiGianNhanCa(LocalDateTime.now());
        newShift.setTrangThai(1); // 1 = Đang làm
        
        // Khởi tạo các mốc tiền ban đầu là 0 (Giữ nguyên để tránh lỗi NullPointer ở các chỗ khác nếu DB yêu cầu)
        newShift.setTienBanDau(BigDecimal.ZERO);
        newShift.setTongThuTrongCa(BigDecimal.ZERO);
        newShift.setTongTienMat(BigDecimal.ZERO);
        newShift.setTongTienChuyenKhoan(BigDecimal.ZERO);
        newShift.setTienPhatSinh(BigDecimal.ZERO);

        return repo.save(newShift);
    }

    /**
     * ĐÓNG CA LÀM VIỆC (Đã bỏ phần kiểm đếm tiền)
     */
    public GiaoCa closeShift(Integer idGiaoCa) {
    GiaoCa giaoCa = repo.findById(idGiaoCa)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc này!"));

    if (giaoCa.getTrangThai() == 0) { 
        throw new RuntimeException("Ca làm việc này đã được kết thúc từ trước!");
    }

    // --- THÊM VALIDATE GIỜ KẾT THÚC ---
    LocalDateTime bayGio = LocalDateTime.now();
    // Lấy giờ kết thúc từ lịch trực (Ca làm việc)
    LocalTime gioKetThucLich = giaoCa.getLichLamViec().getCaLamViec().getThoiGianKetThuc();
    // Chuyển thành LocalDateTime của ngày hôm nay
    LocalDateTime thoiDiemKetThucDuKien = LocalDateTime.of(LocalDate.now(), gioKetThucLich);

    if (bayGio.isBefore(thoiDiemKetThucDuKien)) {
        throw new RuntimeException("Chưa đến giờ kết thúc ca (" + gioKetThucLich + "). Bạn không thể đóng ca sớm!");
    }
    // ---------------------------------

    giaoCa.setThoiGianGiaoCa(bayGio);
    giaoCa.setTrangThai(0); 

    return repo.save(giaoCa);
}
    
}