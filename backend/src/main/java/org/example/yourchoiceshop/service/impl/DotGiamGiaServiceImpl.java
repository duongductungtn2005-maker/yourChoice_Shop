package org.example.yourchoiceshop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.DotGiamGia;
import org.example.yourchoiceshop.repository.ChiTietDotGiamGiaRepository;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.DotGiamGiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaServiceImpl {
    private final DotGiamGiaRepository dotRepo;
    private final ChiTietDotGiamGiaRepository ctDotRepo;
    private final ChiTietSanPhamRepository ctspRepo;

    public Page<DotGiamGia> getAll(String keyword, Integer status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        return dotRepo.search(key, status, startDate, endDate, pageable);
    }

    public DotGiamGia getById(Integer id) {
        return dotRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá: " + id));
    }

    @Transactional
    public DotGiamGia create(DotGiamGiaRequest req) {
        // Validate ngày
        if (req.getNgayBatDau() != null && req.getNgayKetThuc() != null) {
            if (req.getNgayKetThuc().isBefore(req.getNgayBatDau())) {
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        DotGiamGia dot = new DotGiamGia();

        String ma = req.getMaDotGiamGia();
        if (ma == null || ma.trim().isEmpty()) {
            ma = "EVENT" + System.currentTimeMillis();
        }
        if (dotRepo.existsByMaDotGiamGia(ma)) {
            throw new RuntimeException("Mã đợt giảm giá đã tồn tại");
        }
        dot.setMaDotGiamGia(ma);

        mapReqToEntity(req, dot);
        DotGiamGia savedDot = dotRepo.save(dot);

        // Lưu sản phẩm
        saveProductDetails(savedDot, req.getIdChiTietSanPhams());

        return savedDot;
    }

    @Transactional
    public DotGiamGia update(Integer id, DotGiamGiaRequest req) {
        DotGiamGia dot = getById(id);

        if (req.getNgayKetThuc() != null && req.getNgayBatDau() != null && req.getNgayKetThuc().isBefore(req.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        mapReqToEntity(req, dot);

        // Nếu có thay đổi danh sách sản phẩm
        if (req.getIdChiTietSanPhams() != null) {
            // BƯỚC 1: Tìm các chi tiết cũ
            List<ChiTietDotGiamGia> oldDetails = ctDotRepo.findByDotGiamGiaId(id);

            // BƯỚC 2: Gỡ đợt giảm giá khỏi các sản phẩm cũ (Reset về null)
            List<ChiTietSanPham> oldProducts = new ArrayList<>();
            for (ChiTietDotGiamGia detail : oldDetails) {
                ChiTietSanPham sp = detail.getChiTietSanPham();
                if (sp != null) {
                    sp.setDotGiamGia(null); // Xóa liên kết
                    oldProducts.add(sp);
                }
            }
            ctspRepo.saveAll(oldProducts); // Lưu cập nhật gỡ bỏ

            // BƯỚC 3: Xóa bảng trung gian cũ
            ctDotRepo.deleteAll(oldDetails);

            // BƯỚC 4: Thêm sản phẩm mới và cập nhật liên kết mới
            saveProductDetails(dot, req.getIdChiTietSanPhams());
        }

        return dotRepo.save(dot);
    }

    public void delete(Integer id) {
        DotGiamGia dot = getById(id);
        dot.setTrangThai(0); // Xóa mềm
        dotRepo.save(dot);

        // Ngưng kích hoạt các chi tiết liên quan
        List<ChiTietDotGiamGia> details = ctDotRepo.findByDotGiamGiaId(id);

        // Đồng thời gỡ đợt giảm giá khỏi sản phẩm để giá về bình thường
        List<ChiTietSanPham> productsToUpdate = new ArrayList<>();
        for (ChiTietDotGiamGia d : details) {
            d.setTrangThai(0);

            ChiTietSanPham sp = d.getChiTietSanPham();
            if (sp != null) {
                sp.setDotGiamGia(null); // Gỡ bỏ đợt giảm giá
                productsToUpdate.add(sp);
            }
        }
        ctDotRepo.saveAll(details);
        ctspRepo.saveAll(productsToUpdate);
    }

    // --- HÀM QUAN TRỌNG ĐÃ ĐƯỢC SỬA ---
    private void saveProductDetails(DotGiamGia dot, List<Integer> productIds) {
        if (productIds != null && !productIds.isEmpty()) {
            List<ChiTietDotGiamGia> listCT = new ArrayList<>();
            List<ChiTietSanPham> listSp = ctspRepo.findAllById(productIds);

            for (ChiTietSanPham ctsp : listSp) {
                // 1. CẬP NHẬT TRỰC TIẾP VÀO SẢN PHẨM (Để hiển thị giá giảm ngay)
                ctsp.setDotGiamGia(dot);

                // 2. Lưu vào bảng trung gian (Để lưu lịch sử/tracking)
                ChiTietDotGiamGia ctdgg = new ChiTietDotGiamGia();
                ctdgg.setDotGiamGia(dot);
                ctdgg.setChiTietSanPham(ctsp);
                ctdgg.setTrangThai(1);
                listCT.add(ctdgg);
            }

            // Lưu cập nhật cho cả 2 bảng
            ctspRepo.saveAll(listSp); // <-- Quan trọng: Lưu cập nhật id_dot_giam_gia vào bảng SP
            ctDotRepo.saveAll(listCT);
        }
    }

    private void mapReqToEntity(DotGiamGiaRequest req, DotGiamGia entity) {
        entity.setTenDotGiamGia(req.getTenDotGiamGia());
        entity.setGiaTriGiam(req.getGiaTriGiam());
        entity.setLoaiGiamGia(req.getLoaiGiamGia());
        entity.setNgayBatDau(req.getNgayBatDau());
        entity.setNgayKetThuc(req.getNgayKetThuc());
        entity.setTrangThai(req.getTrangThai());
    }

    public byte[] exportExcel() throws IOException {
        List<DotGiamGia> list = dotRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DotGiamGia");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã", "Tên", "Giá trị", "Loại", "Ngày BĐ", "Ngày KT", "Trạng thái"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            int rowIdx = 1;
            for (DotGiamGia item : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaDotGiamGia());
                row.createCell(2).setCellValue(item.getTenDotGiamGia());
                row.createCell(3).setCellValue(item.getGiaTriGiam().doubleValue());
                row.createCell(4).setCellValue(item.getLoaiGiamGia());
                row.createCell(5).setCellValue(item.getNgayBatDau().toString());
                row.createCell(6).setCellValue(item.getNgayKetThuc().toString());
                row.createCell(7).setCellValue(item.getTrangThai() == 1 ? "Hoạt động" : "Ngưng");
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}