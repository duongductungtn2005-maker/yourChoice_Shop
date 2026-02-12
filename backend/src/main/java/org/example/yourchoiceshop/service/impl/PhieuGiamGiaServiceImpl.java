package org.example.yourchoiceshop.service.impl;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.example.yourchoiceshop.entity.PhieuGiamGiaCaNhan;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.repository.PhieuGiamGiaCaNhanRepository;
import org.example.yourchoiceshop.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl {

    @Autowired
    private PhieuGiamGiaRepository repository;
    @Autowired
    private PhieuGiamGiaCaNhanRepository pggCaNhanRepo;

    @Autowired
    private KhachHangRepository khachHangRepo;
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

        String ma = req.getMaPhieuGiamGia();
        if (ma == null || ma.trim().isEmpty()) {
            ma = "PGG" + System.currentTimeMillis();
        } else {
            if (repository.existsByMaPhieuGiamGia(ma)) {
                throw new RuntimeException("Mã phiếu '" + ma + "' đã tồn tại!");
            }
        }
        pgg.setMaPhieuGiamGia(ma);
        pgg.setTenPhieuGiamGia(req.getTenPhieuGiamGia());
        pgg.setLoaiPhieu(req.getLoaiPhieu());
        pgg.setKieu(req.getKieu());
        pgg.setGiaTriGiam(req.getGiaTriGiam());
        pgg.setGiaTriGiamToiDa(req.getGiaTriGiamToiDa());
        pgg.setDonHangToiThieu(req.getDonHangToiThieu());
        pgg.setSoLuong(req.getSoLuong());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());
        pgg.setTrangThai(1);

        // 1. Lưu phiếu cha trước
        PhieuGiamGia savedPgg = repository.save(pgg);

        // 2. LOGIC QUAN TRỌNG CÒN THIẾU: Lưu danh sách khách hàng
        if ("CaNhan".equals(req.getKieu()) && req.getCustomerIds() != null && !req.getCustomerIds().isEmpty()) {
            List<PhieuGiamGiaCaNhan> listCaNhan = new ArrayList<>();

            for (Integer khId : req.getCustomerIds()) {
                KhachHang kh = khachHangRepo.findById(khId).orElse(null);

                // --- SỬA DÒNG IF NÀY ---
                // Chỉ xử lý nếu khách hàng tồn tại VÀ đang Hoạt động (trangThai == 1)
                if (kh != null && kh.getTrangThai() == 1) {
                    // -----------------------

                    PhieuGiamGiaCaNhan pggCn = new PhieuGiamGiaCaNhan();
                    pggCn.setPhieuGiamGia(savedPgg);
                    pggCn.setKhachHang(kh);
                    pggCn.setNgayNhan(LocalDateTime.now());
                    pggCn.setDaSuDung(false);
                    pggCn.setTrangThai(1);
                    pggCn.setMaPhieuKhachHang(savedPgg.getMaPhieuGiamGia() + "-KH" + kh.getId());

                    listCaNhan.add(pggCn);
                }
            }
            // Lưu xuống DB
            if (!listCaNhan.isEmpty()) {
                pggCaNhanRepo.saveAll(listCaNhan);
            }
        }

        return savedPgg;
    }

    // 3. Hàm Bật/Tắt trạng thái (Có logic gia hạn)
    public void toggleStatus(Integer id, LocalDateTime newEndDate) {
        PhieuGiamGia pgg = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        LocalDateTime now = LocalDateTime.now();

        if (pgg.getTrangThai() == 1) {
            pgg.setTrangThai(0);
        } else {
            if (pgg.getNgayKetThuc().isBefore(now)) {
                // SỬA: Check pgg.getKieu() thay vì getLoaiPhieu()
                if ("CongKhai".equals(pgg.getKieu())) {
                    if (newEndDate == null || newEndDate.isBefore(now)) {
                        throw new RuntimeException("Voucher đã hết hạn. Vui lòng nhập ngày kết thúc mới!");
                    }
                    pgg.setNgayKetThuc(newEndDate);
                    pgg.setTrangThai(1);
                } else {
                    throw new RuntimeException("Voucher cá nhân đã hết hạn không thể kích hoạt lại.");
                }
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