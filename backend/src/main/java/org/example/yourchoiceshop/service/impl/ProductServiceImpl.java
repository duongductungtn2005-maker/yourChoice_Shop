package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.BulkUpdateVariantRequest;
import org.example.yourchoiceshop.dto.request.CreateProductRequest;
import org.example.yourchoiceshop.dto.request.ProductVariantRequest;
import org.example.yourchoiceshop.dto.request.UpdateVariantRequest;
import org.example.yourchoiceshop.dto.response.ProductResponse;
import org.example.yourchoiceshop.dto.response.VariantResponse;
import org.example.yourchoiceshop.entity.*;
import org.example.yourchoiceshop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // <--- QUAN TRỌNG: Import thư viện ngày giờ
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietRepo;
    private final ThuongHieuRepository thuongHieuRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final XuatXuRepository xuatXuRepo;
    private final CoAoRepository coAoRepo;
    private final TayAoRepository tayAoRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final HinhAnhRepository hinhAnhRepo;

    // 1. LẤY DANH SÁCH (CHO TRANG LIST)
    public Page<ProductResponse> getAllProducts(
            String keyword, Integer status,
            Integer idThuongHieu, Integer idChatLieu, Integer idXuatXu, Integer idCoAo, Integer idTayAo,
            Pageable pageable) {

        Page<SanPham> page = sanPhamRepo.searchProductsAdvanced(
                keyword, status, idThuongHieu, idChatLieu, idXuatXu, idCoAo, idTayAo, pageable
        );

        return page.map(this::mapToProductResponse);
    }

    // 2. TẠO SẢN PHẨM
    @Transactional
    public SanPham createProduct(CreateProductRequest req) {
        SanPham sp = new SanPham();

        // --- XỬ LÝ MÃ SẢN PHẨM ---
        if (req.getMaSanPham() == null || req.getMaSanPham().isEmpty()) {
            sp.setMaSanPham("SP" + System.currentTimeMillis());
        } else {
            sp.setMaSanPham(req.getMaSanPham());
        }

        // --- GÁN THÔNG TIN CƠ BẢN ---
        sp.setTenSanPham(req.getTenSanPham());
        sp.setMoTaChiTiet(req.getMoTa());
        sp.setTrangThai(1);

        // --- FIX LỖI: GÁN NGÀY TẠO HIỆN TẠI ---
        sp.setNgayTao(LocalDateTime.now());
        // --------------------------------------

        if(req.getIdThuongHieu() != null) sp.setThuongHieu(thuongHieuRepo.findById(req.getIdThuongHieu()).orElse(null));
        if(req.getIdChatLieu() != null) sp.setChatLieu(chatLieuRepo.findById(req.getIdChatLieu()).orElse(null));
        if(req.getIdXuatXu() != null) sp.setXuatXu(xuatXuRepo.findById(req.getIdXuatXu()).orElse(null));
        if(req.getIdCoAo() != null) sp.setCoAo(coAoRepo.findById(req.getIdCoAo()).orElse(null));
        if(req.getIdTayAo() != null) sp.setTayAo(tayAoRepo.findById(req.getIdTayAo()).orElse(null));

        SanPham savedSp = sanPhamRepo.save(sp);

        // --- XỬ LÝ BIẾN THỂ (GIỮ NGUYÊN) ---
        if (req.getVariants() != null && !req.getVariants().isEmpty()) {
            Set<String> uniqueCheck = new HashSet<>();

            for (ProductVariantRequest vReq : req.getVariants()) {
                String key = vReq.getIdMauSac() + "-" + vReq.getIdKichThuoc();

                if (!uniqueCheck.contains(key)) {
                    uniqueCheck.add(key);

                    // Tạo biến thể
                    ChiTietSanPham variant = new ChiTietSanPham();
                    variant.setSanPham(savedSp);
                    if(vReq.getIdMauSac() != null) variant.setMauSac(mauSacRepo.findById(vReq.getIdMauSac()).orElse(null));
                    if(vReq.getIdKichThuoc() != null) variant.setKichThuoc(kichThuocRepo.findById(vReq.getIdKichThuoc()).orElse(null));

                    variant.setMaCtsp(savedSp.getMaSanPham() + "-" + vReq.getIdMauSac() + "-" + vReq.getIdKichThuoc());
                    variant.setSoLuong(vReq.getSoLuong());
                    variant.setGiaNhap(vReq.getGiaNhap());
                    variant.setGiaBan(vReq.getGiaBan());
                    variant.setTrangThai(1);

                    ChiTietSanPham savedVariant = chiTietRepo.save(variant);

                    // Lưu ảnh
                    if (vReq.getListAnh() != null && !vReq.getListAnh().isEmpty()) {
                        List<HinhAnh> listHinhAnh = new ArrayList<>();
                        for (String url : vReq.getListAnh()) {
                            HinhAnh img = new HinhAnh();
                            img.setChiTietSanPham(savedVariant);
                            img.setDuongDanAnh(url);
                            img.setTenAnh("Img-" + savedVariant.getMaCtsp());
                            img.setTrangThai(1);
                            listHinhAnh.add(img);
                        }
                        hinhAnhRepo.saveAll(listHinhAnh);
                    }
                }
            }
        }
        return savedSp;
    }

    // 3. XÓA SẢN PHẨM
    @Transactional
    public void deleteProduct(Integer id) {
        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + id));
        sp.setTrangThai(0);
        sanPhamRepo.save(sp);

        List<ChiTietSanPham> childs = chiTietRepo.findBySanPhamId(id);
        for (ChiTietSanPham child : childs) {
            child.setTrangThai(0);
        }
        chiTietRepo.saveAll(childs);
    }

    // 4. LẤY CHI TIẾT SẢN PHẨM CHA
    public ProductResponse getProductById(Integer id) {
        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + id));
        return mapToProductResponse(sp);
    }

    // 5. LẤY DANH SÁCH BIẾN THỂ
    public List<VariantResponse> getVariantsByProductId(Integer productId) {
        List<ChiTietSanPham> entities = chiTietRepo.findBySanPhamId(productId);

        return entities.stream().map(ct -> {
            VariantResponse.AttributeDTO mauSacDTO = null;
            if (ct.getMauSac() != null) {
                mauSacDTO = new VariantResponse.AttributeDTO(ct.getMauSac().getId(), ct.getMauSac().getTenMauSac());
            }

            VariantResponse.AttributeDTO kichThuocDTO = null;
            if (ct.getKichThuoc() != null) {
                kichThuocDTO = new VariantResponse.AttributeDTO(ct.getKichThuoc().getId(), ct.getKichThuoc().getTenKichThuoc());
            }

            List<String> listAnh = new ArrayList<>();
            if (ct.getHinhAnhs() != null && !ct.getHinhAnhs().isEmpty()) {
                listAnh = ct.getHinhAnhs().stream()
                        .map(HinhAnh::getDuongDanAnh)
                        .collect(Collectors.toList());
            }

            return new VariantResponse(
                    ct.getId(),
                    ct.getMaCtsp(),
                    ct.getSoLuong(),
                    ct.getGiaNhap(),
                    ct.getGiaBan(),
                    ct.getTrangThai(),
                    mauSacDTO,
                    kichThuocDTO,
                    listAnh
            );
        }).collect(Collectors.toList());
    }

    // --- MAP ENTITY -> DTO ---
    private ProductResponse mapToProductResponse(SanPham sp) {
        Integer tongSoLuong = chiTietRepo.sumSoLuongBySanPhamId(sp.getId());

        String dsMauSac = "";
        if (sp.getChiTietSanPhams() != null) {
            dsMauSac = sp.getChiTietSanPhams().stream()
                    .filter(ct -> ct.getMauSac() != null)
                    .map(ct -> ct.getMauSac().getTenMauSac())
                    .distinct().collect(Collectors.joining(", "));
        }

        String dsKichThuoc = "";
        if (sp.getChiTietSanPhams() != null) {
            dsKichThuoc = sp.getChiTietSanPhams().stream()
                    .filter(ct -> ct.getKichThuoc() != null)
                    .map(ct -> ct.getKichThuoc().getTenKichThuoc())
                    .distinct().collect(Collectors.joining(", "));
        }

        return new ProductResponse(
                sp.getId(),
                sp.getMaSanPham(),
                sp.getTenSanPham(),
                sp.getNgayTao(), // Đảm bảo trường này là LocalDateTime
                tongSoLuong != null ? tongSoLuong : 0,
                sp.getTrangThai(),
                sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "",
                sp.getChatLieu() != null ? sp.getChatLieu().getTenChatLieu() : "",
                sp.getXuatXu() != null ? sp.getXuatXu().getTenXuatXu() : "",
                sp.getCoAo() != null ? sp.getCoAo().getTenCoAo() : "",
                sp.getTayAo() != null ? sp.getTayAo().getTenTayAo() : "",
                dsMauSac,
                dsKichThuoc,
                sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null,
                sp.getChatLieu() != null ? sp.getChatLieu().getId() : null,
                sp.getXuatXu() != null ? sp.getXuatXu().getId() : null,
                sp.getCoAo() != null ? sp.getCoAo().getId() : null,
                sp.getTayAo() != null ? sp.getTayAo().getId() : null,
                sp.getMoTaChiTiet()
        );
    }

    // UPDATE VARIANT
    @Transactional
    public void updateVariant(Integer id, UpdateVariantRequest req) {
        ChiTietSanPham variant = chiTietRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể: " + id));

        variant.setGiaNhap(req.getGiaNhap());
        variant.setGiaBan(req.getGiaBan());
        variant.setSoLuong(req.getSoLuong());
        variant.setTrangThai(req.getTrangThai());
        if (req.getIdMauSac() != null) variant.setMauSac(mauSacRepo.findById(req.getIdMauSac()).orElse(null));
        if (req.getIdKichThuoc() != null) variant.setKichThuoc(kichThuocRepo.findById(req.getIdKichThuoc()).orElse(null));

        SanPham parent = variant.getSanPham();
        if (parent != null) {
            sanPhamRepo.save(parent);
        }

        if (req.getListAnh() != null) {
            hinhAnhRepo.deleteByChiTietSanPhamId(id);
            if (!req.getListAnh().isEmpty()) {
                List<HinhAnh> newImages = new ArrayList<>();
                for (String url : req.getListAnh()) {
                    HinhAnh img = new HinhAnh();
                    img.setChiTietSanPham(variant);
                    img.setDuongDanAnh(url);
                    img.setTenAnh("Update-" + System.currentTimeMillis());
                    img.setTrangThai(1);
                    newImages.add(img);
                }
                hinhAnhRepo.saveAll(newImages);
            }
        }
        chiTietRepo.save(variant);
    }

    // BULK UPDATE
    @Transactional
    public void bulkUpdateVariants(List<BulkUpdateVariantRequest> requests) {
        for (BulkUpdateVariantRequest req : requests) {
            ChiTietSanPham variant = chiTietRepo.findById(req.getId()).orElse(null);
            if (variant != null) {
                if (req.getGiaBan() != null) variant.setGiaBan(req.getGiaBan());
                if (req.getSoLuong() != null) variant.setSoLuong(req.getSoLuong());
                chiTietRepo.save(variant);
            }
        }
    }

    // EXPORT EXCEL
    public ByteArrayInputStream exportProductsToExcel() throws IOException {
        List<SanPham> products = sanPhamRepo.findAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách sản phẩm");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã SP", "Tên Sản Phẩm", "Thương Hiệu", "Chất Liệu", "Tổng Tồn", "Trạng Thái"};

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
            for (SanPham sp : products) {
                Row row = sheet.createRow(rowIdx++);
                Integer tongTon = chiTietRepo.sumSoLuongBySanPhamId(sp.getId());

                row.createCell(0).setCellValue(sp.getId());
                row.createCell(1).setCellValue(sp.getMaSanPham());
                row.createCell(2).setCellValue(sp.getTenSanPham());
                row.createCell(3).setCellValue(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "");
                row.createCell(4).setCellValue(sp.getChatLieu() != null ? sp.getChatLieu().getTenChatLieu() : "");
                row.createCell(5).setCellValue(tongTon != null ? tongTon : 0);
                row.createCell(6).setCellValue(sp.getTrangThai() == 1 ? "Đang bán" : "Ngừng bán");
            }
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}