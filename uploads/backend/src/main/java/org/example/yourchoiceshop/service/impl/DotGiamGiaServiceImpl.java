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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        refreshBestDiscountForCampaignProducts(savedDot.getId());

        return savedDot;
    }

    @Transactional
    public DotGiamGia update(Integer id, DotGiamGiaRequest req) {
        DotGiamGia dot = getById(id);
        List<ChiTietSanPham> affectedProducts = getCampaignProducts(id);

        if (req.getNgayKetThuc() != null && req.getNgayBatDau() != null && req.getNgayKetThuc().isBefore(req.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        mapReqToEntity(req, dot);

        // Nếu có thay đổi danh sách sản phẩm
        if (req.getIdChiTietSanPhams() != null) {
            // BƯỚC 1: Tìm các chi tiết cũ
            List<ChiTietDotGiamGia> oldDetails = ctDotRepo.findByDotGiamGiaId(id);

            // BƯỚC 2: Lấy danh sách sản phẩm cũ để tính lại đợt tốt nhất
            List<ChiTietSanPham> oldProducts = new ArrayList<>();
            for (ChiTietDotGiamGia detail : oldDetails) {
                ChiTietSanPham sp = detail.getChiTietSanPham();
                if (sp != null) {
                    oldProducts.add(sp);
                }
            }
            affectedProducts.addAll(oldProducts);

            // BƯỚC 3: Xóa bảng trung gian cũ
            ctDotRepo.deleteAll(oldDetails);

            // BƯỚC 4: Thêm sản phẩm mới và cập nhật liên kết mới
            saveProductDetails(dot, req.getIdChiTietSanPhams());

            if (!req.getIdChiTietSanPhams().isEmpty()) {
                List<ChiTietSanPham> newProducts = ctspRepo.findAllById(req.getIdChiTietSanPhams());
                affectedProducts.addAll(newProducts);
            }
        }

        DotGiamGia savedDot = dotRepo.save(dot);
        if (req.getIdChiTietSanPhams() == null) {
            List<ChiTietDotGiamGia> campaignDetails = ctDotRepo.findByDotGiamGiaId(id);

            Integer nextDetailStatus = Integer.valueOf(1).equals(savedDot.getTrangThai()) ? 1 : 0;
            campaignDetails.forEach(detail -> detail.setTrangThai(nextDetailStatus));
            ctDotRepo.saveAll(campaignDetails);

            campaignDetails.forEach(detail -> {
                if (detail.getChiTietSanPham() != null) {
                    affectedProducts.add(detail.getChiTietSanPham());
                }
            });
        }
        refreshBestDiscountForProducts(affectedProducts);
        return savedDot;
    }

    public void delete(Integer id) {
        DotGiamGia dot = getById(id);
        dot.setTrangThai(0); // Xóa mềm
        dotRepo.save(dot);

        // Ngưng kích hoạt các chi tiết liên quan
        List<ChiTietDotGiamGia> details = ctDotRepo.findByDotGiamGiaId(id);
        List<ChiTietSanPham> affectedProducts = new ArrayList<>();

        for (ChiTietDotGiamGia d : details) {
            d.setTrangThai(0);

            ChiTietSanPham sp = d.getChiTietSanPham();
            if (sp != null) {
                affectedProducts.add(sp);
            }
        }
        ctDotRepo.saveAll(details);
        refreshBestDiscountForProducts(affectedProducts);
    }

    // --- HÀM QUAN TRỌNG ĐÃ ĐƯỢC SỬA ---
    private void saveProductDetails(DotGiamGia dot, List<Integer> productIds) {
    if (productIds != null && !productIds.isEmpty()) {
        List<ChiTietDotGiamGia> listCT = new ArrayList<>();
        List<ChiTietSanPham> listSp = ctspRepo.findAllById(productIds);

        // 1. Lưu vào bảng trung gian trước (để hệ thống biết SP này nằm trong đợt nào)
        for (ChiTietSanPham ctsp : listSp) {
            ChiTietDotGiamGia ctdgg = new ChiTietDotGiamGia();
            ctdgg.setDotGiamGia(dot);
            ctdgg.setChiTietSanPham(ctsp);
            ctdgg.setTrangThai(1);
            listCT.add(ctdgg);
        }
        ctDotRepo.saveAll(listCT);

        // 2. Cập nhật lại "đợt giảm giá tốt nhất" cho TỪNG sản phẩm
        for (ChiTietSanPham ctsp : listSp) {
            updateBestDiscountForProduct(ctsp);
        }
        ctspRepo.saveAll(listSp);
    }
}

// Hàm dùng chung để tính toán lại % tốt nhất
public void updateBestDiscountForProduct(ChiTietSanPham ctsp) {
    if (ctsp == null || ctsp.getId() == null) {
        return;
    }

    List<DotGiamGia> activeDiscounts = ctDotRepo.findBestActiveDiscountForProduct(ctsp.getId());
    
    if (!activeDiscounts.isEmpty()) {
        // Lấy thằng đứng đầu tiên (do đã ORDER BY DESC ở câu Query)
        ctsp.setDotGiamGia(activeDiscounts.get(0));
    } else {
        // Nếu không có đợt nào thỏa mãn thời gian/trạng thái thì clear
        ctsp.setDotGiamGia(null);
    }
}

    private List<ChiTietSanPham> getCampaignProducts(Integer campaignId) {
        List<ChiTietDotGiamGia> details = ctDotRepo.findByDotGiamGiaId(campaignId);
        List<ChiTietSanPham> products = new ArrayList<>();
        for (ChiTietDotGiamGia detail : details) {
            if (detail.getChiTietSanPham() != null) {
                products.add(detail.getChiTietSanPham());
            }
        }
        return products;
    }

    private void refreshBestDiscountForCampaignProducts(Integer campaignId) {
        refreshBestDiscountForProducts(getCampaignProducts(campaignId));
    }

    private void refreshBestDiscountForProducts(List<ChiTietSanPham> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        Map<Integer, ChiTietSanPham> uniqueProducts = new LinkedHashMap<>();
        for (ChiTietSanPham product : products) {
            if (product != null && product.getId() != null) {
                uniqueProducts.put(product.getId(), product);
            }
        }

        if (uniqueProducts.isEmpty()) {
            return;
        }

        List<ChiTietSanPham> toSave = new ArrayList<>(uniqueProducts.values());
        for (ChiTietSanPham product : toSave) {
            updateBestDiscountForProduct(product);
        }
        ctspRepo.saveAll(toSave);
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