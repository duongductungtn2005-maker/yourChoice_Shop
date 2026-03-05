package org.example.yourchoiceshop.service;

import java.io.ByteArrayOutputStream;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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
import org.example.yourchoiceshop.repository.NhanVienRepository; 

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    // ===== 1. TẠO FILE TEMPLATE =====
    public byte[] generateExcelTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet mainSheet = workbook.createSheet("XepLich");
            Sheet hiddenSheet = workbook.createSheet("HiddenData");

            List<CaLamViec> caLamViecs = caLamViecRepository.findAll().stream()
                    .filter(ca -> ca.getTrangThai() != null && ca.getTrangThai() == 1)
                    .collect(Collectors.toList());

            // Ghi Ca làm việc vào sheet ẩn (Cột A)
            for (int i = 0; i < caLamViecs.size(); i++) {
                Row row = hiddenSheet.createRow(i);
                row.createCell(0).setCellValue(caLamViecs.get(i).getTenCa()); 
            }
            workbook.setSheetHidden(1, true); // Giấu sheet data

            // Tạo tiêu đề cho sheet chính (3 Cột)
            Row headerRow = mainSheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Bỏ cột ngày, chỉ còn 3 cột này
            String[] headers = {
                "Mã nhân viên", 
                "Tên nhân viên", 
                "Ca làm việc (Chọn)"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tạo Dropdown cho cột Ca Làm Việc (Cột C - index 2)
            if (!caLamViecs.isEmpty()) {
                DataValidationHelper validationHelper = mainSheet.getDataValidationHelper();
                DataValidationConstraint caConstraint = validationHelper.createFormulaListConstraint("HiddenData!$A$1:$A$" + caLamViecs.size());
                // Áp dụng cho cột C (index 2), từ dòng 2 đến dòng 1000
                CellRangeAddressList caAddressList = new CellRangeAddressList(1, 1000, 2, 2);
                mainSheet.addValidationData(validationHelper.createValidation(caConstraint, caAddressList));
            }

            for (int i = 0; i < headers.length; i++) {
                mainSheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ===== 2. ĐỌC FILE IMPORT =====
    public void importLichLamViec(MultipartFile file, LocalDate ngayLamViec) throws Exception {
        List<String> errorMessages = new ArrayList<>();
        List<LichLamViec> validRecords = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Dòng trong Excel hiển thị cho user (Bắt đầu từ 1, header là 1, data là 2)
                int rowNum = i + 1; 

                Cell maNvCell = row.getCell(0);
                Cell caCell = row.getCell(2); // Cột C

                // 1. Validate: Dữ liệu bị trống
                if (maNvCell == null || caCell == null || 
                    maNvCell.getCellType() == CellType.BLANK || caCell.getCellType() == CellType.BLANK) {
                    errorMessages.add("Dòng " + rowNum + ": Mã nhân viên và Ca làm việc không được để trống.");
                    continue;
                }

                // Xử lý đọc Mã nhân viên
                String maNhanVien = "";
                if (maNvCell.getCellType() == CellType.NUMERIC) {
                    maNhanVien = String.valueOf((long) maNvCell.getNumericCellValue());
                } else {
                    maNhanVien = maNvCell.getStringCellValue().trim();
                }
                
                // Xử lý đọc Tên ca
                String tenCa = caCell.getStringCellValue().trim();

                // 2. Validate: Kiểm tra tồn tại trong DB
                NhanVien nhanVien = nhanVienRepository.findByMaNhanVien(maNhanVien).orElse(null);
                if (nhanVien == null || (nhanVien.getTrangThai() != null && nhanVien.getTrangThai() != 1)) {
                    errorMessages.add("Dòng " + rowNum + ": Không tìm thấy nhân viên mã '" + maNhanVien + "' hoặc đã ngừng hoạt động.");
                    continue; // Chuyển sang dòng tiếp theo
                }

                CaLamViec caLamViec = caLamViecRepository.findFirstByTenCa(tenCa).orElse(null);
                if (caLamViec == null || (caLamViec.getTrangThai() != null && caLamViec.getTrangThai() != 1)) {
                    errorMessages.add("Dòng " + rowNum + ": Không tìm thấy ca làm việc '" + tenCa + "' hoặc ca đã ngừng hoạt động.");
                    continue;
                }

                // 3. Validate: Chống trùng lặp lịch (1 người không thể làm 1 ca 2 lần trong 1 ngày)
                boolean isDuplicate = lichLamViecRepository.existsByNhanVienAndCaLamViecAndNgayLamViec(nhanVien, caLamViec, ngayLamViec);
                if (isDuplicate) {
                    errorMessages.add("Dòng " + rowNum + ": Nhân viên '" + nhanVien.getTenNhanVien() + "' đã được xếp vào ca '" + tenCa + "' trong ngày này rồi.");
                    continue;
                }

                // Nếu vượt qua mọi bài kiểm tra -> Đưa vào danh sách chờ lưu
                LichLamViec lich = new LichLamViec();
                lich.setNgayLamViec(ngayLamViec);
                lich.setNhanVien(nhanVien);
                lich.setCaLamViec(caLamViec);
                validRecords.add(lich);
            }
        }

        // 4. Quyết định Lưu hoặc Báo lỗi
        if (!errorMessages.isEmpty()) {
            // Nối các lỗi lại thành 1 chuỗi, cách nhau bởi dấu xuống dòng
            String combinedErrors = String.join("\n", errorMessages);
            // Ném lỗi ra để Controller bắt lấy
            throw new IllegalArgumentException(combinedErrors); 
        }

        // Nếu không có lỗi nào, tiến hành lưu toàn bộ vào DB
        if (!validRecords.isEmpty()) {
            lichLamViecRepository.saveAll(validRecords);
        }
    }
    
}