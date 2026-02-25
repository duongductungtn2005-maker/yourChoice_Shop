package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.request.HoaDonRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.HoaDonChiTiet;
import org.example.yourchoiceshop.entity.KhachHang; // <--- THÊM IMPORT NÀY
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.HoaDonChiTietRepository;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.KhachHangRepository; // <--- THÊM IMPORT NÀY
import org.example.yourchoiceshop.service.HoaDonService; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    
    // --- THÊM REPO KHÁCH HÀNG VÀO ĐÂY ĐỂ TÌM KHÁCH HÀNG ---
    private final KhachHangRepository khachHangRepo; 

    @Override
    public Page<HoaDonResponse> getOrders(String keyword, Integer status, String type, LocalDateTime from,
                                          LocalDateTime to, Pageable pageable) {
        Page<HoaDon> page = hoaDonRepo.searchOrders(keyword, status, type, from, to, pageable);

        return page.map(hd -> {
            HoaDonResponse res = new HoaDonResponse();
            res.setMaHoaDon(hd.getMaHoaDon());
            int totalItems = hd.getHoaDonChiTiets() != null
                    ? hd.getHoaDonChiTiets().stream().mapToInt(HoaDonChiTiet::getSoLuong).sum()
                    : 0;
            res.setTongSanPham(totalItems);
            res.setTongTienSauGiam(hd.getTongTienSauGiam());
            res.setTenKhachHang(hd.getTenNguoiNhan() != null ? hd.getTenNguoiNhan()
                    : (hd.getKhachHang() != null ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ"));
            res.setNgayTao(hd.getNgayTao());
            res.setLoaiHoaDon(convertTypeToDisplay(hd.getLoaiHoaDon()));
            res.setTrangThai(hd.getTrangThai());
            return res;
        });
    }

    @Override
    public void updateOrderInfo(String maHoaDon, HoaDonRequest req) {
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        // Cập nhật thông tin nhận hàng
        hd.setTenNguoiNhan(req.getTenNguoiNhan());
        hd.setSdtNguoiNhan(req.getSdtNguoiNhan());
        hd.setDiaChiNguoiNhan(req.getDiaChiNguoiNhan());

        hoaDonRepo.save(hd);
    }

    @Override
    public HoaDonDetailResponse getOrderDetail(String maHoaDon) {
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        HoaDonDetailResponse res = new HoaDonDetailResponse();
        res.setMaHoaDon(hd.getMaHoaDon());
        res.setTenKhachHang(hd.getKhachHang() != null ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ");
        res.setLoaiHoaDon(convertTypeToDisplay(hd.getLoaiHoaDon()));
        res.setTrangThai(hd.getTrangThai());
        res.setNgayTao(hd.getNgayTao());

        res.setTongTien(hd.getTongTien());
        res.setTienGiam(
                hd.getTienGiamGia() != null ? hd.getTienGiamGia() : BigDecimal.ZERO);
        res.setPhiVanChuyen(hd.getPhiVanChuyen());
        res.setTongTienSauGiam(hd.getTongTienSauGiam());

        HoaDonDetailResponse.ThongTinNhanHang info = new HoaDonDetailResponse.ThongTinNhanHang();
        info.setTenNguoiNhan(hd.getTenNguoiNhan());
        info.setSdt(hd.getSdtNguoiNhan());
        info.setDiaChi(hd.getDiaChiNguoiNhan());
        res.setThongTinNhanHang(info);

        List<HoaDonDetailResponse.SanPhamHoaDonResponse> products = new ArrayList<>();
        if (hd.getHoaDonChiTiets() != null) {
            products = hd.getHoaDonChiTiets().stream().map(ct -> {
                HoaDonDetailResponse.SanPhamHoaDonResponse p = new HoaDonDetailResponse.SanPhamHoaDonResponse();
                p.setTenSanPham(ct.getChiTietSanPham().getSanPham().getTenSanPham());
                p.setSize(ct.getChiTietSanPham().getKichThuoc().getTenKichThuoc());
                p.setMauSac(ct.getChiTietSanPham().getMauSac().getTenMauSac());
                p.setSoLuong(ct.getSoLuong());
                p.setDonGia(ct.getDonGia());
                p.setThanhTien(ct.getThanhTien());
                return p;
            }).collect(Collectors.toList());
        }
        res.setSanPhamHoaDon(products);

        List<HoaDonDetailResponse.LichSuThanhToanResponse> payments = new ArrayList<>();
        if (hd.getLichSuThanhToans() != null) {
            payments = hd.getLichSuThanhToans().stream().map(ls -> {
                HoaDonDetailResponse.LichSuThanhToanResponse pay = new HoaDonDetailResponse.LichSuThanhToanResponse();
                pay.setSoTien(ls.getSoTien());
                pay.setNgayThanhToan(ls.getNgayThanhToan());
                pay.setHinhThucThanhToan(convertPaymentMethod(ls.getHinhThucThanhToan()));
                return pay;
            }).collect(Collectors.toList());
        }
        res.setLichSuThanhToan(payments);

        return res;
    }

    private String convertTypeToDisplay(String dbType) {
        if ("TRUC_TUYEN".equalsIgnoreCase(dbType))
            return "Trực tuyến";
        if ("TAI_QUAY".equalsIgnoreCase(dbType))
            return "Tại quầy";
        return dbType;
    }

    private String convertPaymentMethod(String dbMethod) {
        if ("TIEN_MAT".equalsIgnoreCase(dbMethod))
            return "Tiền mặt";
        if ("CHUYEN_KHOAN".equalsIgnoreCase(dbMethod))
            return "Chuyển khoản";
        return dbMethod;
    }

    @Override
    public void updateStatus(String maHoaDon, Integer newStatus) {
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));
        hd.setTrangThai(newStatus);
        hoaDonRepo.save(hd);
    }

    @Override
    @Transactional
    public void createOrderAtCounter(CreateOrderRequest req) {

        // 1. Tạo hóa đơn
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setTrangThai(5);
        hd.setLoaiHoaDon("TAI_QUAY");

        hd.setTenNguoiNhan(req.getTenKhachHang() != null ? req.getTenKhachHang() : "Khách lẻ");
        hd.setSdtNguoiNhan(req.getSoDienThoai());
        hd.setDiaChiNguoiNhan(req.getDiaChi());
        hd.setEmailKhachHang(req.getEmail());

        // ---> QUAN TRỌNG: GÁN KHÓA NGOẠI KHÁCH HÀNG VÀO HÓA ĐƠN <---
        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            hd.setKhachHang(kh); // Lưu cái này thì bảng Khách Hàng mới lên số liệu!
        }

        hoaDonRepo.save(hd);

        // 2. Lưu chi tiết hóa đơn
        BigDecimal tongTien = BigDecimal.ZERO;

        for (CreateOrderRequest.CartItem item : req.getItems()) {

            ChiTietSanPham sp = chiTietSanPhamRepo
                    .findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            if (sp.getSoLuong() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + sp.getSanPham().getTenSanPham() + " không đủ tồn kho");
            }

            sp.setSoLuong(sp.getSoLuong() - item.getSoLuong());
            chiTietSanPhamRepo.save(sp);

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setChiTietSanPham(sp);
            ct.setSoLuong(item.getSoLuong());
            ct.setDonGia(BigDecimal.valueOf(item.getDonGia()));

            BigDecimal thanhTien = ct.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));

            ct.setThanhTien(thanhTien);
            tongTien = tongTien.add(thanhTien);

            hoaDonChiTietRepo.save(ct);
        }

        // 3. Cập nhật tiền
        hd.setTongTien(tongTien);

        BigDecimal tienGiam = req.getTienGiamGia() != null ? req.getTienGiamGia() : BigDecimal.ZERO;

        hd.setTienGiamGia(tienGiam);
        hd.setTongTienSauGiam(tongTien.subtract(tienGiam));

        hoaDonRepo.save(hd);
    }

    @Override
    public byte[] exportExcel(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to) {
        List<HoaDon> list = hoaDonRepo.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

            Row headerRow = sheet.createRow(0);
            String[] columns = { "STT", "Mã HĐ", "Khách hàng", "Ngày tạo", "Loại", "Trạng thái", "Tổng tiền" };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (HoaDon hd : list) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(hd.getMaHoaDon());
                row.createCell(2).setCellValue(hd.getTenNguoiNhan() != null ? hd.getTenNguoiNhan() : "Khách lẻ");
                row.createCell(3).setCellValue(hd.getNgayTao().toString());
                row.createCell(4).setCellValue(convertTypeToDisplay(hd.getLoaiHoaDon()));
                row.createCell(5).setCellValue(convertStatusToText(hd.getTrangThai())); 
                row.createCell(6).setCellValue(hd.getTongTienSauGiam().doubleValue());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất Excel: " + e.getMessage());
        }
    }

    private String convertStatusToText(Integer status) {
        if (status == 1) return "Chờ xác nhận";
        if (status == 4) return "Hoàn thành";
        return "Khác";
    }
}