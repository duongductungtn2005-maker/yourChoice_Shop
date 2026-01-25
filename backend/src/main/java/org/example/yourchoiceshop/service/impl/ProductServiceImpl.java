package org.example.yourchoiceshop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.CreateProductRequest;
import org.example.yourchoiceshop.dto.response.ProductResponse;
import org.example.yourchoiceshop.entity.*;
import org.example.yourchoiceshop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    // 1. Inject đầy đủ các Repository cần dùng
    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietRepo;
    private final ThuongHieuRepository thuongHieuRepo;
    private final XuatXuRepository xuatXuRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final CoAoRepository coAoRepo;
    private final TayAoRepository tayAoRepo;
    private final MauSacRepository mauSacRepo;       // Cần cái này để fix lỗi dòng setMauSac
    private final KichThuocRepository kichThuocRepo; // Cần cái này để fix lỗi dòng setKichThuoc

    // 2. Lấy danh sách sản phẩm
    public Page<ProductResponse> getAllProducts(String keyword, Pageable pageable) {
        Page<SanPham> page = sanPhamRepo.searchSanPhams(keyword, pageable);
        return page.map(this::mapToResponse);
    }

    // 3. Thêm mới sản phẩm (FIXED)
    @Transactional
    public SanPham createProduct(CreateProductRequest request) {
        // A. Lưu bảng cha SanPham
        SanPham sanPham = new SanPham();
        // Tự sinh mã nếu không có: SP + timestamp
        sanPham.setMaSanPham(request.getMaSanPham() != null ? request.getMaSanPham() : "SP" + System.currentTimeMillis());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTaChiTiet(request.getMoTa());
        sanPham.setTrangThai(1);

        // Map các thuộc tính (Dùng orElse(null) để tránh lỗi nếu không tìm thấy ID)
        if(request.getIdThuongHieu() != null) sanPham.setThuongHieu(thuongHieuRepo.findById(request.getIdThuongHieu()).orElse(null));
        if(request.getIdXuatXu() != null) sanPham.setXuatXu(xuatXuRepo.findById(request.getIdXuatXu()).orElse(null));
        if(request.getIdChatLieu() != null) sanPham.setChatLieu(chatLieuRepo.findById(request.getIdChatLieu()).orElse(null));
        if(request.getIdCoAo() != null) sanPham.setCoAo(coAoRepo.findById(request.getIdCoAo()).orElse(null));
        if(request.getIdTayAo() != null) sanPham.setTayAo(tayAoRepo.findById(request.getIdTayAo()).orElse(null));

        // Lưu sản phẩm cha
        SanPham savedSanPham = sanPhamRepo.save(sanPham);

        // B. Lưu danh sách biến thể (ChiTietSanPham)
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // [FIX] Khởi tạo listChiTiet trước vòng lặp
            List<ChiTietSanPham> listChiTiet = new ArrayList<>();

            request.getVariants().forEach(variantReq -> {
                ChiTietSanPham chiTiet = new ChiTietSanPham();
                chiTiet.setSanPham(savedSanPham); // Link ngược lại cha

                // Logic sinh mã chi tiết: SPxxx-IDMau-IDSize
                String maCTSP = savedSanPham.getMaSanPham() + "-" + variantReq.getIdMauSac() + "-" + variantReq.getIdKichThuoc();
                chiTiet.setMaCtsp(maCTSP);

                chiTiet.setSoLuong(variantReq.getSoLuong());
                chiTiet.setGiaNhap(variantReq.getGiaNhap());
                chiTiet.setGiaBan(variantReq.getGiaBan());
                chiTiet.setTrangThai(1);

                // [FIX] Set Màu sắc & Size (Đảm bảo repo đã được inject ở trên)
                chiTiet.setMauSac(mauSacRepo.findById(variantReq.getIdMauSac()).orElse(null));
                chiTiet.setKichThuoc(kichThuocRepo.findById(variantReq.getIdKichThuoc()).orElse(null));

                // Thêm vào list
                listChiTiet.add(chiTiet);
            });

            // Lưu tất cả biến thể cùng lúc
            chiTietRepo.saveAll(listChiTiet);
        }

        return savedSanPham;
    }

    // 4. Lấy chi tiết
    public SanPham getProductById(Integer id) {
        return sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
    }

    // 5. Xóa mềm
    @Transactional
    public void deleteProduct(Integer id) {
        SanPham sanPham = getProductById(id);
        sanPham.setTrangThai(0);
        sanPhamRepo.save(sanPham);

        List<ChiTietSanPham> childs = sanPham.getChiTietSanPhams();
        if(childs != null) {
            childs.forEach(c -> c.setTrangThai(0));
            chiTietRepo.saveAll(childs);
        }
    }

    // Helper: Map Entity -> Response DTO
    private ProductResponse mapToResponse(SanPham sp) {
        int tongTon = 0;
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;

        List<ChiTietSanPham> variants = sp.getChiTietSanPhams();
        if (variants != null && !variants.isEmpty()) {
            tongTon = variants.stream().mapToInt(v -> v.getSoLuong() == null ? 0 : v.getSoLuong()).sum();
            minPrice = variants.stream().map(ChiTietSanPham::getGiaBan).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            maxPrice = variants.stream().map(ChiTietSanPham::getGiaBan).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }

        return ProductResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .moTa(sp.getMoTaChiTiet())
                .tenThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "N/A")
                .tenChatLieu(sp.getChatLieu() != null ? sp.getChatLieu().getTenChatLieu() : "N/A")
                .tenXuatXu(sp.getXuatXu() != null ? sp.getXuatXu().getTenXuatXu() : "N/A")
                .tenCoAo(sp.getCoAo() != null ? sp.getCoAo().getTenCoAo() : "N/A")
                .tenTayAo(sp.getTayAo() != null ? sp.getTayAo().getTenTayAo() : "N/A")
                .tongSoLuongTon(tongTon)
                .soLuongBienThe(variants != null ? variants.size() : 0)
                .giaThapNhat(minPrice)
                .giaCaoNhat(maxPrice)
                .trangThai(sp.getTrangThai())
                .ngayTao(sp.getNgayTao())
                .build();
    }
}