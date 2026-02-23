package org.example.yourchoiceshop.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.request.HoaDonRequest;
import org.example.yourchoiceshop.dto.request.PaymentRequest;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.repository.HoaDonChiTietRepository;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.example.yourchoiceshop.service.HoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;

    @Override
    public Page<QuanLyDonHangResponse> getDanhSachDonHang(QuanLyDonHangRequest request) {
        return Page.empty();
    }

    @Override
    public Page<QuanLyDonHangResponse> searchDonHang(QuanLyDonHangRequest request) {
        return Page.empty();
    }

    @Override
    public Page<HoaDonResponse> getOrders(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<HoaDon> page = hoaDonRepository.searchOrders(keyword, status, type, from, to, pageable);

        return page.map(hd -> {
            HoaDonResponse res = new HoaDonResponse();
            res.setId(hd.getId());
            res.setMaHoaDon(hd.getMaHoaDon());
            res.setNgayTao(hd.getNgayTao());
            res.setTrangThai(hd.getTrangThai());
            res.setLoaiHoaDon(hd.getLoaiHoaDon());
            res.setTongTienSauGiam(hd.getTongTienSauGiam() != null ? hd.getTongTienSauGiam() : BigDecimal.ZERO);
            res.setTenKhachHang(hd.getTenNguoiNhan());
            res.setTongSanPham(0);
            return res;
        });
    }

    @Override
    public HoaDonDetailResponse getOrderDetail(String maHoaDon) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        // 1. Lấy danh sách sản phẩm
        List<SanPhamHoaDonResponse> sanPhamList = new ArrayList<>();
        try {
            List<SanPhamHoaDonResponse> dbList = hoaDonChiTietRepository.findSanPhamByDonHang(hd.getId());
            if (dbList != null) {
                sanPhamList = dbList;
            }
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn sản phẩm: " + e.getMessage());
        }

        // 2. Lấy danh sách lịch sử thanh toán
        List<HoaDonDetailResponse.LichSuThanhToanResponse> lichSuThanhToanList = new ArrayList<>();
        try {
            List<LichSuThanhToan> lsttEntities = lichSuThanhToanRepository.findByHoaDonId(hd.getId());
            if (lsttEntities != null) {
                lichSuThanhToanList = lsttEntities.stream().map(item -> {
                    HoaDonDetailResponse.LichSuThanhToanResponse dto = new HoaDonDetailResponse.LichSuThanhToanResponse();
                    dto.setSoTien(item.getSoTien());
                    dto.setNgayThanhToan(item.getNgayThanhToan());
                    String method = item.getHinhThucThanhToan();
                    if ("TIEN_MAT".equals(method)) dto.setHinhThucThanhToan("Tiền mặt");
                    else if ("CHUYEN_KHOAN".equals(method)) dto.setHinhThucThanhToan("Chuyển khoản");
                    else dto.setHinhThucThanhToan(method);
                    return dto;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Lỗi lịch sử thanh toán: " + e.getMessage());
        }

        HoaDonDetailResponse response = new HoaDonDetailResponse();
        response.setId(hd.getId());
        response.setMaHoaDon(hd.getMaHoaDon());
        response.setTenKhachHang(hd.getTenNguoiNhan());
        response.setSdtKhachHang(hd.getSdtNguoiNhan());
        response.setDiaChi(hd.getDiaChiNguoiNhan());
        response.setTrangThai(hd.getTrangThai());
        response.setLoaiHoaDon(hd.getLoaiHoaDon());
        response.setGhiChu(hd.getGhiChu());
        response.setNgayTao(hd.getNgayTao());

        // 3. Xử lý tiền (Fix lỗi getTongTienSauGiam)
        response.setTongTienHang(hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
        response.setTienGiam(hd.getTienGiamGia() != null ? hd.getTienGiamGia() : BigDecimal.ZERO);
        response.setPhiVanChuyen(hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO);
        response.setTongTienSauGiam(hd.getTongTienSauGiam() != null ? hd.getTongTienSauGiam() : BigDecimal.ZERO);

        response.setSanPhamHoaDon(sanPhamList);
        response.setLichSuThanhToan(lichSuThanhToanList);

        return response;
    }

    @Override
    public void updateStatus(String maHoaDon, Integer newStatus) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        hd.setTrangThai(newStatus);
        hoaDonRepository.save(hd);
    }

    @Override
    public void updateOrderInfo(String maHoaDon, HoaDonRequest request) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        if (request.getTenNguoiNhan() != null) hd.setTenNguoiNhan(request.getTenNguoiNhan());
        if (request.getSdtNguoiNhan() != null) hd.setSdtNguoiNhan(request.getSdtNguoiNhan());
        if (request.getDiaChiNguoiNhan() != null) hd.setDiaChiNguoiNhan(request.getDiaChiNguoiNhan());
        if (request.getGhiChu() != null) hd.setGhiChu(request.getGhiChu());
        hoaDonRepository.save(hd);
    }

    @Override
    public void createOrderAtCounter(CreateOrderRequest req) {
        HoaDon hd = new HoaDon();
        hd.setLoaiHoaDon("TAI_QUAY");
        hd.setTrangThai(5);
        hd.setNgayTao(LocalDateTime.now());
        hoaDonRepository.save(hd);
    }

    @Override
    public byte[] exportExcel(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to) {
        List<HoaDon> list = hoaDonRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Mã HĐ", "Khách hàng", "Ngày tạo", "Tổng tiền", "Trạng thái"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (HoaDon hd : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(hd.getMaHoaDon());
                row.createCell(1).setCellValue(hd.getTenNguoiNhan());
                row.createCell(2).setCellValue(hd.getNgayTao() != null ? hd.getNgayTao().toString() : "");
                row.createCell(3).setCellValue(hd.getTongTien() != null ? hd.getTongTien().doubleValue() : 0);
                row.createCell(4).setCellValue(hd.getTrangThai());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xuất Excel: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void confirmPayment(String maHoaDon, PaymentRequest request) {
        HoaDon hd = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        LichSuThanhToan lstt = new LichSuThanhToan();
        lstt.setHoaDon(hd);
        lstt.setSoTien(request.getSoTien());
        lstt.setNgayThanhToan(LocalDateTime.now());
        lstt.setHinhThucThanhToan(request.getHinhThucThanhToan());
        lstt.setLoaiThanhToan("THANH_TOAN_HOA_DON");
        lstt.setTrangThai(1);
        lstt.setGhiChu(request.getGhiChu());
        lichSuThanhToanRepository.save(lstt);
        hd.setTrangThai(5);
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setHinhThucThanhToan(request.getHinhThucThanhToan());
        hoaDonRepository.save(hd);
    }
}