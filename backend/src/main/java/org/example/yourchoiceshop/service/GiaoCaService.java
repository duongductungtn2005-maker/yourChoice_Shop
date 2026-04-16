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
    return repo.findFirstByNhanVienTrongCa_IdAndTrangThaiOrderByThoiGianNhanCaDesc(
        idNhanVien, 1 // 1 = đang làm
    );
}

    /**
     * MỞ CA LÀM VIỆC
     */
    public GiaoCa openShift(Integer idNhanVien, Integer idLichLamViec) {
        
        // ❗ Không cho mở lại cùng 1 lịch
boolean daMo = repo.existsByNhanVienTrongCa_IdAndLichLamViec_Id(idNhanVien, idLichLamViec);

if (daMo) {
    throw new RuntimeException("Ca này đã được mở trước đó, không thể mở lại!");
}
    // 1. ❌ Kiểm tra ca đang mở
    if (getCurrentActiveShift(idNhanVien).isPresent()) {
        throw new RuntimeException("Bạn đang có một ca làm việc chưa kết thúc!");
    }

    // 2. ❌ QUAN TRỌNG: Kiểm tra đã từng mở ca cho lịch này chưa
    Optional<GiaoCa> existingShift = repo.findByLichLamViecId(idLichLamViec);

    if (existingShift.isPresent()) {
        throw new RuntimeException("Bạn đã hoàn thành ca làm việc này rồi, không thể mở lại!");
    }

    // 3. Lấy thông tin
    LichLamViec lichLamViec = lichLamViecRepo.findById(idLichLamViec)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin lịch làm việc!"));
        
    NhanVien nhanVien = nhanVienRepo.findById(idNhanVien)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên!"));

    // 4. Khởi tạo Giao Ca
    GiaoCa newShift = new GiaoCa();
    newShift.setMaGiaoCa("GC_" + System.currentTimeMillis()); 
    newShift.setNhanVienTrongCa(nhanVien);
    newShift.setLichLamViec(lichLamViec);
    newShift.setThoiGianNhanCa(LocalDateTime.now());
    newShift.setTrangThai(1); // 1 = Đang làm

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
    LocalTime gioKetThucLich = giaoCa.getLichLamViec()
        .getCaLamViec()
        .getThoiGianKetThuc();

if (gioKetThucLich == null) {
    throw new RuntimeException("Ca làm việc chưa có giờ kết thúc!");
}
    // Chuyển thành LocalDateTime của ngày hôm nay
    LocalDateTime thoiDiemKetThucDuKien = LocalDateTime.of(LocalDate.now(), gioKetThucLich);

    boolean dongSom = bayGio.isBefore(thoiDiemKetThucDuKien);
    giaoCa.setDongSom(dongSom); // nếu có field
    // ---------------------------------

    giaoCa.setThoiGianGiaoCa(bayGio);
    giaoCa.setTrangThai(0); 

    return repo.save(giaoCa);
}
    
}