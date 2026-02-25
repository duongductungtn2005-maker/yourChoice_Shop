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
import org.springframework.transaction.annotation.Transactional; // NHỚ THÊM IMPORT NÀY

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

    // 2. Hàm tạo mới
    @Transactional
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
        pgg.setMoTa(req.getMoTa()); // ĐÃ BỔ SUNG LƯU MÔ TẢ
        pgg.setTrangThai(1);

        // 1. Lưu phiếu cha trước
        PhieuGiamGia savedPgg = repository.save(pgg);

        // 2. Lưu danh sách khách hàng
        if ("CaNhan".equals(req.getKieu()) && req.getCustomerIds() != null && !req.getCustomerIds().isEmpty()) {
            List<PhieuGiamGiaCaNhan> listCaNhan = new ArrayList<>();

            for (Integer khId : req.getCustomerIds()) {
                KhachHang kh = khachHangRepo.findById(khId).orElse(null);

                // Chỉ xử lý nếu khách hàng tồn tại VÀ đang Hoạt động (trangThai == 1)
                if (kh != null && kh.getTrangThai() == 1) {
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

    // -------------------------------------------------------------
    // 3. API CẬP NHẬT (MỚI THÊM VÀO ĐÂY)
    // -------------------------------------------------------------
    @Transactional // Phải có @Transactional vì thao tác xóa và lưu nhiều bảng
    public PhieuGiamGia update(Integer id, PhieuGiamGiaRequest req) {
        // 1. Tìm phiếu cũ trong DB
        PhieuGiamGia voucher = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        // 2. Đè dữ liệu mới lên
        voucher.setTenPhieuGiamGia(req.getTenPhieuGiamGia());
        voucher.setLoaiPhieu(req.getLoaiPhieu());
        voucher.setKieu(req.getKieu());
        voucher.setGiaTriGiam(req.getGiaTriGiam());
        voucher.setGiaTriGiamToiDa(req.getGiaTriGiamToiDa());
        voucher.setDonHangToiThieu(req.getDonHangToiThieu());
        voucher.setSoLuong(req.getSoLuong());
        voucher.setNgayBatDau(req.getNgayBatDau());
        voucher.setNgayKetThuc(req.getNgayKetThuc());
        voucher.setMoTa(req.getMoTa()); // Cập nhật cả mô tả

        // 3. Lưu thông tin phiếu vào DB
        PhieuGiamGia savedVoucher = repository.save(voucher);

        // 4. Xử lý logic khách hàng nếu chuyển kiểu hoặc đổi danh sách
        // Xóa hết danh sách map cũ của phiếu này đi trước cho sạch sẽ
        List<PhieuGiamGiaCaNhan> oldList = pggCaNhanRepo.findByPhieuGiamGiaId(id);
        if (!oldList.isEmpty()) {
            pggCaNhanRepo.deleteAll(oldList);
        }

        // Thêm lại danh sách khách hàng mới nếu chọn Cá Nhân
        if ("CaNhan".equals(req.getKieu()) && req.getCustomerIds() != null && !req.getCustomerIds().isEmpty()) {
            List<PhieuGiamGiaCaNhan> listCaNhan = new ArrayList<>();

            for (Integer khId : req.getCustomerIds()) {
                KhachHang kh = khachHangRepo.findById(khId).orElse(null);

                if (kh != null && kh.getTrangThai() == 1) {
                    PhieuGiamGiaCaNhan pggCn = new PhieuGiamGiaCaNhan();
                    pggCn.setPhieuGiamGia(savedVoucher);
                    pggCn.setKhachHang(kh);
                    pggCn.setNgayNhan(LocalDateTime.now());
                    pggCn.setDaSuDung(false);
                    pggCn.setTrangThai(1);
                    pggCn.setMaPhieuKhachHang(savedVoucher.getMaPhieuGiamGia() + "-KH" + kh.getId());

                    listCaNhan.add(pggCn);
                }
            }
            if (!listCaNhan.isEmpty()) {
                pggCaNhanRepo.saveAll(listCaNhan);
            }
        }

        return savedVoucher;
    }

    // 4. Hàm Bật/Tắt trạng thái (Có logic gia hạn)
    public void toggleStatus(Integer id, LocalDateTime newEndDate) {
        PhieuGiamGia pgg = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        LocalDateTime now = LocalDateTime.now();

        if (pgg.getTrangThai() == 1) {
            pgg.setTrangThai(0);
        } else {
            if (pgg.getNgayKetThuc().isBefore(now)) {
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

    // 5. Xuất Excel
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