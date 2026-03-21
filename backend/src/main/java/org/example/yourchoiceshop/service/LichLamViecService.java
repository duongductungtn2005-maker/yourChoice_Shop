package org.example.yourchoiceshop.service;

import java.io.ByteArrayOutputStream;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.LichLamViecRequest;

// THÊM CÁC DÒNG IMPORT REPOSITORY NÀY
import org.example.yourchoiceshop.repository.LichLamViecRepository; 
import org.example.yourchoiceshop.repository.CaLamViecRepository; 
import org.example.yourchoiceshop.repository.GiaoCaRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository; 

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LichLamViecService {

    private final LichLamViecRepository lichLamViecRepository;
    private final CaLamViecRepository caLamViecRepository; 
    private final NhanVienRepository nhanVienRepository;   
    private final GiaoCaRepository giaoCaRepo; // Repository để quản lý GiaoCa

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

    // ===== 1. TẠO FILE TEMPLATE =====
    public byte[] generateExcelTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet mainSheet = workbook.createSheet("XepLich");
            Sheet hiddenSheet = workbook.createSheet("HiddenData");

            List<CaLamViec> caLamViecs = caLamViecRepository.findAll().stream()
                    .filter(ca -> ca.getTrangThai() != null && ca.getTrangThai() == 1)
                    .collect(Collectors.toList());

            // Ghi Ca làm việc vào sheet ẩn
            for (int i = 0; i < caLamViecs.size(); i++) {
                Row row = hiddenSheet.createRow(i);
                row.createCell(0).setCellValue(caLamViecs.get(i).getTenCa());
            }
            workbook.setSheetHidden(1, true);

            // Tiêu đề
            Row headerRow = mainSheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Ngày làm việc (dd/MM/yyyy)", "Mã nhân viên", "Ca làm việc (Chọn)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Set Format cột A (Ngày tháng) thành Text hoặc Date chuẩn để user dễ nhập
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            mainSheet.setDefaultColumnStyle(0, dateStyle);

            // Tạo Dropdown cho cột C (Ca làm việc - Index 2)
            if (!caLamViecs.isEmpty()) {
                DataValidationHelper validationHelper = mainSheet.getDataValidationHelper();
                DataValidationConstraint caConstraint = validationHelper.createFormulaListConstraint("HiddenData!$A$1:$A$" + caLamViecs.size());
                CellRangeAddressList caAddressList = new CellRangeAddressList(1, 2000, 2, 2);
                mainSheet.addValidationData(validationHelper.createValidation(caConstraint, caAddressList));
            }

            // Chỉnh độ rộng cột cho đẹp
            mainSheet.setColumnWidth(0, 6000); 
            mainSheet.setColumnWidth(1, 5000);
            mainSheet.setColumnWidth(2, 6000);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ===== 2. ĐỌC FILE IMPORT =====
    // LƯU Ý: Đã bỏ tham số ngayLamViec truyền từ Web vào, vì giờ sẽ đọc ngày từ file Excel
    public void importLichLamViec(MultipartFile file) throws Exception {
        List<String> errorMessages = new ArrayList<>();
        List<LichLamViec> validRecords = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                int rowNum = i + 1;

                Cell dateCell = row.getCell(0);
                Cell maNvCell = row.getCell(1);
                Cell caCell = row.getCell(2);

                if (dateCell == null || maNvCell == null || caCell == null ||
                    dateCell.getCellType() == CellType.BLANK || maNvCell.getCellType() == CellType.BLANK || caCell.getCellType() == CellType.BLANK) {
                    errorMessages.add("Dòng " + rowNum + ": Thiếu dữ liệu (Ngày, Mã NV hoặc Ca làm việc).");
                    continue;
                }

                // --- 1. Đọc và Validate Ngày ---
                LocalDate ngayLamViec = null;
                try {
                    if (DateUtil.isCellDateFormatted(dateCell)) {
                        ngayLamViec = dateCell.getLocalDateTimeCellValue().toLocalDate();
                    } else {
                        // Nếu user lỡ tay nhập text "05/03/2026"
                        String dateStr = dateCell.getStringCellValue().trim();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        ngayLamViec = LocalDate.parse(dateStr, formatter);
                    }
                } catch (Exception e) {
                    errorMessages.add("Dòng " + rowNum + ": Sai định dạng ngày. Vui lòng nhập chuẩn dd/MM/yyyy.");
                    continue;
                }

                // --- 2. Đọc Mã NV & Ca làm việc ---
                String maNhanVien = (maNvCell.getCellType() == CellType.NUMERIC) ? 
                                    String.valueOf((long) maNvCell.getNumericCellValue()) : maNvCell.getStringCellValue().trim();
                String tenCa = caCell.getStringCellValue().trim();

                // --- 3. Validate Database ---
                NhanVien nhanVien = nhanVienRepository.findByMaNhanVien(maNhanVien).orElse(null);
                if (nhanVien == null || (nhanVien.getTrangThai() != null && nhanVien.getTrangThai() != 1)) {
                    errorMessages.add("Dòng " + rowNum + ": Mã nhân viên '" + maNhanVien + "' không hợp lệ.");
                    continue;
                }

                CaLamViec caLamViec = caLamViecRepository.findFirstByTenCa(tenCa).orElse(null);
                if (caLamViec == null || (caLamViec.getTrangThai() != null && caLamViec.getTrangThai() != 1)) {
                    errorMessages.add("Dòng " + rowNum + ": Ca làm việc '" + tenCa + "' không hợp lệ.");
                    continue;
                }

                // --- 4. Validate Trùng lặp ---
                if (lichLamViecRepository.existsByNhanVienAndCaLamViecAndNgayLamViec(nhanVien, caLamViec, ngayLamViec)) {
                    errorMessages.add("Dòng " + rowNum + ": NV '" + nhanVien.getTenNhanVien() + "' đã có ca '" + tenCa + "' vào ngày " + ngayLamViec.toString() + ".");
                    continue;
                }

                LichLamViec lich = new LichLamViec();
                lich.setNgayLamViec(ngayLamViec);
                lich.setNhanVien(nhanVien);
                lich.setCaLamViec(caLamViec);
                validRecords.add(lich);
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errorMessages));
        }

        if (!validRecords.isEmpty()) {
            lichLamViecRepository.saveAll(validRecords);
        }
    }
    @Transactional
    public String copyLichTuTuanTruoc(LocalDate ngayTrongTuanHienTai) {
        // 1. Xác định Thứ 2 và Chủ Nhật của tuần HIỆN TẠI (tuần đích)
        LocalDate thuHaiHienTai = ngayTrongTuanHienTai.with(java.time.DayOfWeek.MONDAY);
        
        // 2. Xác định Thứ 2 và Chủ Nhật của tuần TRƯỚC (tuần nguồn)
        LocalDate thuHaiTuanTruoc = thuHaiHienTai.minusWeeks(1);
        LocalDate chuNhatTuanTruoc = thuHaiTuanTruoc.plusDays(6);

        // 3. Lấy toàn bộ lịch làm việc của tuần trước
        List<LichLamViec> lichTuanTruoc = lichLamViecRepository.findByNgayLamViecBetween(thuHaiTuanTruoc, chuNhatTuanTruoc);

        if (lichTuanTruoc.isEmpty()) {
            throw new RuntimeException("Tuần trước không có lịch làm việc nào để sao chép!");
        }

        List<LichLamViec> lichMoiDeLuu = new ArrayList<>();
        int soLuongCopyThanhCong = 0;

        // 4. Duyệt qua từng lịch tuần trước, cộng thêm 7 ngày
        for (LichLamViec lichCu : lichTuanTruoc) {
            LocalDate ngayMoi = lichCu.getNgayLamViec().plusWeeks(1);

            // Kiểm tra xem ngày mới này, nhân viên này, ca này đã được xếp lịch chưa (tránh trùng lặp)
            boolean daTonTai = lichLamViecRepository.existsByNhanVienAndCaLamViecAndNgayLamViec(
                    lichCu.getNhanVien(), lichCu.getCaLamViec(), ngayMoi
            );

            if (!daTonTai) {
                LichLamViec lichMoi = new LichLamViec();
                lichMoi.setNgayLamViec(ngayMoi);
                lichMoi.setNhanVien(lichCu.getNhanVien());
                lichMoi.setCaLamViec(lichCu.getCaLamViec());
                
                lichMoiDeLuu.add(lichMoi);
                soLuongCopyThanhCong++;
            }
        }

        if (soLuongCopyThanhCong == 0) {
            return "Dữ liệu tuần này đã tồn tại, không có lịch mới nào được sao chép thêm.";
        }

        // 5. Lưu toàn bộ xuống Database
        lichLamViecRepository.saveAll(lichMoiDeLuu);
        
        return "Đã sao chép thành công " + soLuongCopyThanhCong + " ca làm việc từ tuần trước!";
    }
    public LichLamViec layLichLamViecHomNayCuaNhanVien(String username) {
        // Không dùng SecurityContextHolder nữa, nhận trực tiếp từ Controller truyền vào
        LocalDate today = LocalDate.now();
        return lichLamViecRepository.findLichCuaNhanVienTrongNgay(username, today).orElse(null);
    }
    @Transactional
    public void deleteLich(Long idLich) {
        // 1. Tìm xem có ca nào đang OPEN liên quan đến lịch này không
        // Giả sử quan hệ GiaoCa - LichLamViec là 1-1 hoặc N-1
        List<GiaoCa> activeShifts = GiaoCaRepository.findAllByLichLamViecIdAndTrangThai(idLich, "OPEN");

        // 2. Nếu có, đóng ca cưỡng bức trước khi xóa lịch
        for (GiaoCa ca : activeShifts) {
            ca.setTrangThai("CLOSED");
            ca.setThoiGianKetThuc(LocalDateTime.now());
            ca.setGhiChu("Hệ thống tự động đóng do quản lý xóa lịch trực.");
            GiaoCaRepository.save(ca);
        }

        // 3. Cuối cùng mới xóa lịch
        lichLamViecRepository.deleteById(idLich);
    }
    @Transactional
public void xoaLichLamViec(Integer idLich) {
    // 1. Kiểm tra xem lịch này có đang được mở ca (Check-in) không
    Optional<GiaoCa> caDangMo = giaoCaRepo.findByLichLamViec_IdAndTrangThai(idLich, 1);

    // 2. Nếu có ca đang mở, đóng nó lại trước khi xóa lịch
    caDangMo.ifPresent(ca -> {
        ca.setThoiGianGiaoCa(LocalDateTime.now());
        ca.setTrangThai(0); // Đóng ca
        ca.setGhiChu("Hệ thống tự động đóng ca do lịch làm việc bị xóa.");
        giaoCaRepo.save(ca);
    });

    // 3. Tiến hành xóa lịch
    lichLamViecRepository.deleteById(idLich);
}
}