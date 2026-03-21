package org.example.yourchoiceshop.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.dto.request.LichLamViecRequest;

import org.example.yourchoiceshop.repository.LichLamViecRepository; 
import org.example.yourchoiceshop.repository.CaLamViecRepository; 
import org.example.yourchoiceshop.repository.GiaoCaRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository; 

@Service
@RequiredArgsConstructor
public class LichLamViecService {

    private final LichLamViecRepository lichLamViecRepository;
    private final CaLamViecRepository caLamViecRepository;   
    private final NhanVienRepository nhanVienRepository;     
    private final GiaoCaRepository giaoCaRepo; 

    public List<LichLamViec> getLichLamViec(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return lichLamViecRepository.findByNgayLamViecBetweenOrderByNgayLamViecAsc(startDate, endDate);
        }
        return lichLamViecRepository.findAllByOrderByNgayLamViecAsc();
    }

    @Transactional 
    public LichLamViec create(LichLamViecRequest request) {
        CaLamViec ca = caLamViecRepository.findById(request.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ca làm việc"));
        
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân viên"));

        LichLamViec lich = new LichLamViec();
        lich.setCaLamViec(ca);
        lich.setNhanVien(nv);
        lich.setNgayLamViec(request.getNgayLamViec());

        return lichLamViecRepository.save(lich);
    }

    public LichLamViec update(Integer id, LichLamViecRequest request) {
        LichLamViec lich = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch làm việc"));
        
        CaLamViec ca = caLamViecRepository.findById(request.getCaLamViecId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ca làm việc"));
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhân viên"));

        lich.setCaLamViec(ca);
        lich.setNhanVien(nv);
        lich.setNgayLamViec(request.getNgayLamViec());

        return lichLamViecRepository.save(lich);
    }

    // Gộp hàm delete cũ vào đây, bổ sung logic đóng GiaoCa
    @Transactional
    public void delete(Integer id) {
        if (!lichLamViecRepository.existsById(id)) {
            throw new RuntimeException("Lịch làm việc không tồn tại");
        }

        // 1. Kiểm tra xem lịch này có đang được mở ca không
        Optional<GiaoCa> caDangMo = giaoCaRepo.findByLichLamViec_IdAndTrangThai(id, 1);

        // 2. Nếu có ca đang mở, đóng nó lại trước khi xóa lịch
        caDangMo.ifPresent(ca -> {
            ca.setThoiGianGiaoCa(LocalDateTime.now());
            ca.setTrangThai(0); 
            ca.setGhiChu("Hệ thống tự động đóng ca do quản lý xóa lịch trực.");
            giaoCaRepo.save(ca);
        });

        // 3. Xóa lịch
        lichLamViecRepository.deleteById(id);
    }

    public byte[] generateExcelTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet mainSheet = workbook.createSheet("XepLich");
            Sheet hiddenSheet = workbook.createSheet("HiddenData");

            List<CaLamViec> caLamViecs = caLamViecRepository.findAll().stream()
                    .filter(ca -> ca.getTrangThai() != null && ca.getTrangThai() == 1)
                    .collect(Collectors.toList());

            for (int i = 0; i < caLamViecs.size(); i++) {
                Row row = hiddenSheet.createRow(i);
                row.createCell(0).setCellValue(caLamViecs.get(i).getTenCa());
            }
            workbook.setSheetHidden(1, true);

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

            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            mainSheet.setDefaultColumnStyle(0, dateStyle);

            if (!caLamViecs.isEmpty()) {
                DataValidationHelper validationHelper = mainSheet.getDataValidationHelper();
                DataValidationConstraint caConstraint = validationHelper.createFormulaListConstraint("HiddenData!$A$1:$A$" + caLamViecs.size());
                CellRangeAddressList caAddressList = new CellRangeAddressList(1, 2000, 2, 2);
                mainSheet.addValidationData(validationHelper.createValidation(caConstraint, caAddressList));
            }

            mainSheet.setColumnWidth(0, 6000); 
            mainSheet.setColumnWidth(1, 5000);
            mainSheet.setColumnWidth(2, 6000);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional
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

                LocalDate ngayLamViec = null;
                try {
                    if (DateUtil.isCellDateFormatted(dateCell)) {
                        ngayLamViec = dateCell.getLocalDateTimeCellValue().toLocalDate();
                    } else {
                        String dateStr = dateCell.getStringCellValue().trim();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        ngayLamViec = LocalDate.parse(dateStr, formatter);
                    }
                } catch (Exception e) {
                    errorMessages.add("Dòng " + rowNum + ": Sai định dạng ngày. Vui lòng nhập chuẩn dd/MM/yyyy.");
                    continue;
                }

                String maNhanVien = (maNvCell.getCellType() == CellType.NUMERIC) ? 
                                    String.valueOf((long) maNvCell.getNumericCellValue()) : maNvCell.getStringCellValue().trim();
                String tenCa = caCell.getStringCellValue().trim();

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
        } // Đóng try-with-resources của Workbook ở đây

        // Xử lý nốt phần lưu hoặc báo lỗi nằm TRONG hàm importLichLamViec
        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errorMessages));
        }

        if (!validRecords.isEmpty()) {
            lichLamViecRepository.saveAll(validRecords);
        }
    }

    @Transactional
    public String copyLichTuTuanTruoc(LocalDate ngayTrongTuanHienTai) {
        LocalDate thuHaiHienTai = ngayTrongTuanHienTai.with(java.time.DayOfWeek.MONDAY);
        LocalDate thuHaiTuanTruoc = thuHaiHienTai.minusWeeks(1);
        LocalDate chuNhatTuanTruoc = thuHaiTuanTruoc.plusDays(6);

        List<LichLamViec> lichTuanTruoc = lichLamViecRepository.findByNgayLamViecBetween(thuHaiTuanTruoc, chuNhatTuanTruoc);

        if (lichTuanTruoc.isEmpty()) {
            throw new RuntimeException("Tuần trước không có lịch làm việc nào để sao chép!");
        }

        List<LichLamViec> lichMoiDeLuu = new ArrayList<>();
        int soLuongCopyThanhCong = 0;

        for (LichLamViec lichCu : lichTuanTruoc) {
            LocalDate ngayMoi = lichCu.getNgayLamViec().plusWeeks(1);

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

        lichLamViecRepository.saveAll(lichMoiDeLuu);
        
        return "Đã sao chép thành công " + soLuongCopyThanhCong + " ca làm việc từ tuần trước!";
    }

    public LichLamViec layLichLamViecHomNayCuaNhanVien(String username) {
        LocalDate today = LocalDate.now();
        return lichLamViecRepository.findLichCuaNhanVienTrongNgay(username, today).orElse(null);
    }

    public Optional<LichLamViec> findById(Integer id) {
        return lichLamViecRepository.findById(id);
    }
}
