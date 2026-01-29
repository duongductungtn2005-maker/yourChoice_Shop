package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.DotGiamGiaRequest;
import org.example.yourchoiceshop.dto.response.SaleProductResponse;
import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.DotGiamGia;
import org.example.yourchoiceshop.repository.ChiTietDotGiamGiaRepository;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.DotGiamGiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaServiceImpl {

    private final DotGiamGiaRepository dotRepo;
    private final ChiTietDotGiamGiaRepository ctDotRepo;
    private final ChiTietSanPhamRepository ctspRepo;

    // 1. Lấy danh sách (Có tìm kiếm & Lọc)
    public Page<DotGiamGia> getAll(String keyword, Integer status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        return dotRepo.search(key, status, startDate, endDate, pageable);
    }

    // 2. Lấy chi tiết đợt giảm giá theo ID
    public DotGiamGia getById(Integer id) {
        return dotRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá: " + id));
    }

    // 3. --- HÀM QUAN TRỌNG: LẤY DANH SÁCH SẢN PHẨM & TÍNH GIÁ ---
    public List<SaleProductResponse> getProductsBySaleId(Integer saleId) {
        DotGiamGia sale = getById(saleId);

        // Bước 1: Tìm xem đợt này đã được gán sản phẩm nào chưa
        List<ChiTietDotGiamGia> listLinks = ctDotRepo.findActiveProductsBySaleId(saleId);
        List<SaleProductResponse> responses = new ArrayList<>();

        // Bước 2: LOGIC TỰ ĐỘNG (Fallback)
        // Nếu chưa gán sản phẩm nào (List rỗng) -> Lấy TẤT CẢ sản phẩm trong kho ra hiển thị
        if (listLinks.isEmpty()) {
            List<ChiTietSanPham> allProducts = ctspRepo.findAll();
            for (ChiTietSanPham ctsp : allProducts) {
                // Chỉ lấy sản phẩm đang hoạt động (trangThai = 1)
                if (ctsp.getTrangThai() != null && ctsp.getTrangThai() == 1) {
                    // mapToResponse(Sản phẩm, Đợt giảm giá, Số lượng áp dụng)
                    // Số lượng = null nghĩa là lấy theo tồn kho thực tế
                    responses.add(mapToResponse(ctsp, sale, null));
                }
            }
        } else {
            // Nếu đã gán sản phẩm rồi thì chỉ hiển thị những sản phẩm đó
            for (ChiTietDotGiamGia link : listLinks) {
                if (link.getChiTietSanPham() != null) {
                    responses.add(mapToResponse(link.getChiTietSanPham(), sale, link.getSoLuongApDung()));
                }
            }
        }

        return responses;
    }

    // Hàm phụ: Chuyển đổi Entity sang DTO và Tính giá
    private SaleProductResponse mapToResponse(ChiTietSanPham ctsp, DotGiamGia sale, Integer soLuongApDung) {
        SaleProductResponse res = new SaleProductResponse();
        res.setId(ctsp.getId()); // Dùng ID sản phẩm làm key
        res.setMaSanPham(ctsp.getMaCtsp());
        // Nếu số lượng áp dụng là null (do lấy tất cả), hiển thị số lượng tồn kho
        res.setSoLuongApDung(soLuongApDung != null ? soLuongApDung : ctsp.getSoLuong());

        // Check null an toàn để không bị lỗi
        res.setTenSanPham(ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Unknown");
        res.setMauSac(ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMauSac() : "-");
        res.setKichThuoc(ctsp.getKichThuoc() != null ? ctsp.getKichThuoc().getTenKichThuoc() : "-");

        // Giá gốc
        BigDecimal giaGoc = ctsp.getGiaBan() != null ? ctsp.getGiaBan() : BigDecimal.ZERO;
        res.setGiaGoc(giaGoc);

        // --- TÍNH GIÁ SAU GIẢM ---
        BigDecimal giaSauGiam = giaGoc;
        if (sale.getGiaTriGiam() != null) {
            if ("VND".equalsIgnoreCase(sale.getLoaiGiamGia())) {
                // Giảm tiền mặt
                giaSauGiam = giaSauGiam.subtract(sale.getGiaTriGiam());
            } else {
                // Giảm phần trăm
                BigDecimal phanTram = sale.getGiaTriGiam().divide(BigDecimal.valueOf(100));
                BigDecimal tienGiam = giaSauGiam.multiply(phanTram);
                giaSauGiam = giaSauGiam.subtract(tienGiam);
            }
        }

        // Không để giá âm
        if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            giaSauGiam = BigDecimal.ZERO;
        }
        res.setGiaSauGiam(giaSauGiam);

        return res;
    }

    // 4. Tạo mới đợt giảm giá
    @Transactional
    public DotGiamGia create(DotGiamGiaRequest req) {
        // 1. Validate ngày
        if (req.getNgayBatDau() != null && req.getNgayKetThuc() != null) {
            if (req.getNgayKetThuc().isBefore(req.getNgayBatDau())) {
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        // 2. Tạo Đợt giảm giá
        DotGiamGia dot = new DotGiamGia();

        // Tự sinh mã nếu rỗng
        String ma = req.getMaDotGiamGia();
        if (ma == null || ma.trim().isEmpty()) {
            ma = "EVENT" + System.currentTimeMillis();
        }
        dot.setMaDotGiamGia(ma);

        // Map dữ liệu
        dot.setTenDotGiamGia(req.getTenDotGiamGia());
        dot.setGiaTriGiam(req.getGiaTriGiam());
        dot.setLoaiGiamGia(req.getLoaiGiamGia());
        dot.setNgayBatDau(req.getNgayBatDau());
        dot.setNgayKetThuc(req.getNgayKetThuc());
        dot.setTrangThai(1); // Mặc định hoạt động

        DotGiamGia savedDot = dotRepo.save(dot);

        // 3. Lưu các sản phẩm được chọn (QUAN TRỌNG)
        if (req.getIdChiTietSanPhams() != null && !req.getIdChiTietSanPhams().isEmpty()) {
            List<ChiTietDotGiamGia> listLinks = new ArrayList<>();
            // Tìm tất cả chi tiết sản phẩm theo list ID gửi lên
            List<ChiTietSanPham> products = ctspRepo.findAllById(req.getIdChiTietSanPhams());

            for (ChiTietSanPham ctsp : products) {
                ChiTietDotGiamGia link = new ChiTietDotGiamGia();
                link.setDotGiamGia(savedDot);
                link.setChiTietSanPham(ctsp);
                link.setTrangThai(1);
                link.setSoLuongApDung(ctsp.getSoLuong()); // Mặc định áp dụng hết số lượng tồn
                listLinks.add(link);
            }
            ctDotRepo.saveAll(listLinks);
        }

        return savedDot;
    }

    // 5. Cập nhật
    @Transactional
    public DotGiamGia update(Integer id, DotGiamGiaRequest req) {
        DotGiamGia dot = getById(id);
        if (req.getNgayKetThuc() != null && req.getNgayBatDau() != null && req.getNgayKetThuc().isBefore(req.getNgayBatDau())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        mapReqToEntity(req, dot);

        if (req.getIdChiTietSanPhams() != null) {
            List<ChiTietDotGiamGia> oldDetails = ctDotRepo.findByDotGiamGiaId(id);
            ctDotRepo.deleteAll(oldDetails);
            saveProductDetails(dot, req.getIdChiTietSanPhams());
        }
        return dotRepo.save(dot);
    }

    // 6. Xóa mềm (Dừng hoạt động)
    @Transactional
    public void delete(Integer id) {
        DotGiamGia dot = getById(id);
        dot.setTrangThai(0);
        dotRepo.save(dot);

        List<ChiTietDotGiamGia> details = ctDotRepo.findByDotGiamGiaId(id);
        for (ChiTietDotGiamGia d : details) {
            d.setTrangThai(0);
        }
        ctDotRepo.saveAll(details);
    }

    // Helper: Map request to entity
    private void mapReqToEntity(DotGiamGiaRequest req, DotGiamGia entity) {
        entity.setTenDotGiamGia(req.getTenDotGiamGia());
        entity.setGiaTriGiam(req.getGiaTriGiam());
        entity.setLoaiGiamGia(req.getLoaiGiamGia());
        entity.setNgayBatDau(req.getNgayBatDau());
        entity.setNgayKetThuc(req.getNgayKetThuc());
        entity.setTrangThai(req.getTrangThai());
    }

    // Helper: Lưu chi tiết sản phẩm
    private void saveProductDetails(DotGiamGia dot, List<Integer> productIds) {
        if (productIds != null && !productIds.isEmpty()) {
            List<ChiTietDotGiamGia> listCT = new ArrayList<>();
            List<ChiTietSanPham> listSp = ctspRepo.findAllById(productIds);
            for (ChiTietSanPham ctsp : listSp) {
                ChiTietDotGiamGia ctdgg = new ChiTietDotGiamGia();
                ctdgg.setDotGiamGia(dot);
                ctdgg.setChiTietSanPham(ctsp);
                ctdgg.setTrangThai(1);
                ctdgg.setSoLuongApDung(ctsp.getSoLuong()); // Mặc định lấy theo tồn kho
                listCT.add(ctdgg);
            }
            ctDotRepo.saveAll(listCT);
        }
    }

    // 7. Xuất Excel
    public byte[] exportExcel() throws IOException {
        List<DotGiamGia> list = dotRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DotGiamGia");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã", "Tên", "Giá trị", "Loại", "Ngày BĐ", "Ngày KT", "Trạng thái"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
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