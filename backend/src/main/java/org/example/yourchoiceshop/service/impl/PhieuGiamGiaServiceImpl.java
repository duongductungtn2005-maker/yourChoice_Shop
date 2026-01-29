package org.example.yourchoiceshop.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.repository.PhieuGiamGiaRepository;
import org.example.yourchoiceshop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl {

    @Autowired
    private PhieuGiamGiaRepository repository;

    @Autowired
    private KhachHangRepository khachHangRepository; // Để lấy email khách hàng
    @Autowired
    private EmailService emailService;

    // @Autowired
    // private EmailService emailService; // Uncomment khi bạn tích hợp gửi mail thật

    // 1. Hàm lấy danh sách
    public Page<PhieuGiamGia> getAll(String keyword, Integer status, String scope, Pageable pageable) {
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String kieu = (scope != null && !scope.trim().isEmpty()) ? scope : null;
        return repository.search(key, status, kieu, pageable);
    }

    // 2. Hàm tạo mới (Đã cập nhật logic gửi mail)
    @Transactional
    public PhieuGiamGia create(PhieuGiamGiaRequest req) {
        PhieuGiamGia pgg = new PhieuGiamGia();

        // Xử lý Mã phiếu
        String ma = req.getMaPhieuGiamGia();
        if (ma == null || ma.trim().isEmpty()) {
            ma = "PGG" + System.currentTimeMillis();
        } else {
            if (repository.existsByMaPhieuGiamGia(ma)) {
                throw new RuntimeException("Mã phiếu '" + ma + "' đã tồn tại!");
            }
        }
        pgg.setMaPhieuGiamGia(ma);

        // Map dữ liệu
        pgg.setTenPhieuGiamGia(req.getTenPhieuGiamGia());
        pgg.setLoaiPhieu(req.getLoaiPhieu()); // PhanTram / TienMat
        pgg.setKieu(req.getKieu());           // CongKhai / CaNhan
        pgg.setGiaTriGiam(req.getGiaTriGiam());
        pgg.setGiaTriGiamToiDa(req.getGiaTriGiamToiDa());
        pgg.setDonHangToiThieu(req.getDonHangToiThieu());
        pgg.setSoLuong(req.getSoLuong());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());
        pgg.setTrangThai(1);

        PhieuGiamGia savedVoucher = repository.save(pgg);

        // LOGIC GỬI EMAIL CHO KHÁCH HÀNG (Nếu là phiếu cá nhân)
        if ("CaNhan".equals(req.getKieu()) && req.getCustomerIds() != null && !req.getCustomerIds().isEmpty()) {
            List<KhachHang> customers = khachHangRepository.findAllById(req.getCustomerIds());

            for (KhachHang kh : customers) {
                if (kh.getEmail() != null && !kh.getEmail().isEmpty()) {

                    // GỌI HÀM MỚI TẠI ĐÂY
                    emailService.sendVoucherEmail(kh.getEmail(), savedVoucher);

                    System.out.println(">>> Đã gửi voucher " + savedVoucher.getMaPhieuGiamGia() + " tới " + kh.getEmail());
                }
            }
        }

        return savedVoucher;
    }

    // 3. Toggle Status
    public void toggleStatus(Integer id, LocalDateTime newEndDate) {
        PhieuGiamGia pgg = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        LocalDateTime now = LocalDateTime.now();

        if (pgg.getTrangThai() == 1) {
            pgg.setTrangThai(0);
        } else {
            if (pgg.getNgayKetThuc().isBefore(now)) {
                if (newEndDate == null || newEndDate.isBefore(now)) {
                    throw new RuntimeException("Voucher đã hết hạn. Vui lòng nhập ngày kết thúc mới!");
                }
                pgg.setNgayKetThuc(newEndDate);
                pgg.setTrangThai(1);
            } else {
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