package org.example.yourchoiceshop.service.impl;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.example.yourchoiceshop.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PhieuGiamGiaServiceImpl {

    @Autowired
    private PhieuGiamGiaRepository repository;

    // 1. Hàm lấy danh sách (Fix lỗi tham số search)
    public Page<PhieuGiamGia> getAll(String keyword, Integer status, String scope, Pageable pageable) {
        // Chuyển chuỗi rỗng thành null để Query JPQL hoạt động đúng
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String kieu = (scope != null && !scope.trim().isEmpty()) ? scope : null;

        return repository.search(key, status, kieu, pageable);
    }

    // 2. Hàm tạo mới// hoặc public PhieuGiamGia create(...)
    public PhieuGiamGia create(PhieuGiamGiaRequest req) {
        PhieuGiamGia pgg = new PhieuGiamGia();

        // --- ĐOẠN CODE FIX LỖI ---
        String ma = req.getMaPhieuGiamGia();

        // 1. Nếu mã bị null hoặc rỗng -> Tự sinh mã
        if (ma == null || ma.trim().isEmpty()) {
            ma = "PGG" + System.currentTimeMillis(); // Ví dụ: PGG1705829100
        } else {
            // 2. Nếu có nhập mã -> Kiểm tra xem đã tồn tại chưa
            if (repository.existsByMaPhieuGiamGia(ma)) {
                throw new RuntimeException("Mã phiếu '" + ma + "' đã tồn tại, vui lòng chọn mã khác!");
            }
        }
        pgg.setMaPhieuGiamGia(ma);
        // -------------------------

        // Map các trường còn lại
        pgg.setTenPhieuGiamGia(req.getTenPhieuGiamGia());
        pgg.setLoaiPhieu(req.getLoaiPhieu()); // Đúng: setLoaiPhieu khớp với DB        pgg.setLoaiPhieu(req.getLoaiPhieu());
        pgg.setGiaTriGiam(req.getGiaTriGiam());
        pgg.setSoLuong(req.getSoLuong());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());

        // Mặc định trạng thái là Hoạt động (1) khi mới tạo
        pgg.setTrangThai(1);

        return repository.save(pgg);
    }

    // 3. Hàm Bật/Tắt trạng thái (Có logic gia hạn)
    public void toggleStatus(Integer id, LocalDateTime newEndDate) {
        PhieuGiamGia pgg = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        LocalDateTime now = LocalDateTime.now();

        if (pgg.getTrangThai() == 1) {
            // Đang chạy -> Tắt
            pgg.setTrangThai(0);
        } else {
            // Đang tắt -> Bật lại
            if (pgg.getNgayKetThuc().isBefore(now)) {
                // Đã hết hạn
                if ("CongKhai".equals(pgg.getLoaiPhieu())) {
                    if (newEndDate == null || newEndDate.isBefore(now)) {
                        throw new RuntimeException("Voucher đã hết hạn. Vui lòng nhập ngày kết thúc mới!");
                    }
                    pgg.setNgayKetThuc(newEndDate); // Gia hạn
                    pgg.setTrangThai(1);
                } else {
                    throw new RuntimeException("Voucher cá nhân đã hết hạn không thể kích hoạt lại.");
                }
            } else {
                // Còn hạn -> Chỉ việc bật
                pgg.setTrangThai(1);
            }
        }
        repository.save(pgg);
    }

    // 4. Xuất Excel
    public byte[] exportExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vouchers");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã");
            header.createCell(1).setCellValue("Tên");
            header.createCell(2).setCellValue("Kiểu");
            header.createCell(3).setCellValue("Ngày KT");

            int rowIdx = 1;
            for (PhieuGiamGia item : repository.findAll()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getMaPhieuGiamGia());
                row.createCell(1).setCellValue(item.getTenPhieuGiamGia());
                row.createCell(2).setCellValue(item.getLoaiPhieu());
                row.createCell(3).setCellValue(item.getNgayKetThuc().toString());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}