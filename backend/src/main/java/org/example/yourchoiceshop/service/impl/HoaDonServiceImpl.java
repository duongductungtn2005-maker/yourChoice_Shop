package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.HoaDonChiTiet;
import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.HoaDonChiTietRepository;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.example.yourchoiceshop.service.HoaDonService; // <--- Import Interface này
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.example.yourchoiceshop.dto.request.HoaDonRequest; // <--- Import DTO mớiimport org.apache.poi.ss.usermodel.*;
import org.example.yourchoiceshop.dto.request.PhieuGiamGiaRequest;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
// import org.springframework.data.jpa.repository.EntityGraph; // Thêm import này

@Service
@RequiredArgsConstructor
@Transactional
public class HoaDonServiceImpl implements HoaDonService { // <--- THÊM implements HoaDonService

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final NhanVienRepository nhanVienRepo; // Thêm Repository này để lấy tên nhân viên
    private final LichSuThanhToanRepository lichSuThanhToanRepo; // Thêm Repository này để lấy lịch sử thanh toán

    @Override // <--- Thêm Override cho chắc chắn
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

            res.setTenKhachHang(
                    hd.getTenNguoiNhan() != null
                            ? hd.getTenNguoiNhan()
                            : (hd.getKhachHang() != null ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ"));

            // ✅ CHỖ QUYẾT ĐỊNH
            res.setTenNhanVien(
                    hd.getNhanVien() != null
                            ? hd.getNhanVien().getTenNhanVien()
                            : null);

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

    @Override // <--- Thêm Override
    public HoaDonDetailResponse getOrderDetail(String maHoaDon) {
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        HoaDonDetailResponse res = new HoaDonDetailResponse();
        res.setMaHoaDon(hd.getMaHoaDon());
        res.setTenKhachHang(hd.getKhachHang() != null ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ");
        res.setEmailKhachHang(hd.getEmailKhachHang()); // ✅ DÒNG QUYẾT ĐỊNH
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
        return dbType; // Nếu không khớp thì trả về nguyên gốc
    }

    private String convertPaymentMethod(String dbMethod) {
        if ("TIEN_MAT".equalsIgnoreCase(dbMethod))
            return "Tiền mặt";
        if ("CHUYEN_KHOAN".equalsIgnoreCase(dbMethod))
            return "Chuyển khoản";
        return dbMethod;
    }

    @Override // <--- Override từ Interface
    public void updateStatus(String maHoaDon, Integer newStatus) {
        // 1. Tìm hóa đơn theo mã
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        // 2. Cập nhật trạng thái mới
        hd.setTrangThai(newStatus);

        // 3. Lưu vào Database
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

        hd.setTenNguoiNhan(
                req.getTenKhachHang() != null ? req.getTenKhachHang() : "Khách lẻ");
        hd.setSdtNguoiNhan(req.getSoDienThoai());
        hd.setDiaChiNguoiNhan(req.getDiaChi());
        hd.setEmailKhachHang(req.getEmail());
        hd.setGhiChu(req.getGhiChu());

        // fallback từ khách hàng (nếu có)
        if (hd.getEmailKhachHang() == null && hd.getKhachHang() != null) {
            hd.setEmailKhachHang(hd.getKhachHang().getEmail());
        }
        // ✅ GÁN NHÂN VIÊN THEO MÃ
        if (req.getMaNhanVien() != null && !req.getMaNhanVien().isBlank()) {
            hd.setNhanVien(
                    nhanVienRepo.findByMaNhanVien(req.getMaNhanVien())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        hoaDonRepo.save(hd);

        // 2. Lưu chi tiết hóa đơn
        BigDecimal tongTien = BigDecimal.ZERO;

        for (CreateOrderRequest.CartItem item : req.getItems()) {

            ChiTietSanPham sp = chiTietSanPhamRepo
                    .findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // ✅ Check tồn kho
            if (sp.getSoLuong() < item.getSoLuong()) {
                throw new RuntimeException(
                        "Sản phẩm " + sp.getSanPham().getTenSanPham() + " không đủ tồn kho");
            }

            // ✅ Trừ tồn kho
            sp.setSoLuong(sp.getSoLuong() - item.getSoLuong());
            chiTietSanPhamRepo.save(sp);

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setChiTietSanPham(sp);
            ct.setSoLuong(item.getSoLuong());
            ct.setDonGia(BigDecimal.valueOf(item.getDonGia()));

            BigDecimal thanhTien = ct.getDonGia()
                    .multiply(BigDecimal.valueOf(item.getSoLuong()));

            ct.setThanhTien(thanhTien);
            tongTien = tongTien.add(thanhTien);

            hoaDonChiTietRepo.save(ct);
        }
        // 3. Cập nhật tiền
        hd.setTongTien(tongTien);

        BigDecimal tienGiam = BigDecimal.ZERO;

        if (req.getPhieuGiamGia() != null) {
            for (PhieuGiamGiaRequest p : req.getPhieuGiamGia()) {

                // Check hiệu lực cơ bản (nên có)
                if (p.getTrangThai() != null && p.getTrangThai() != 1)
                    continue;

                if ("PhanTram".equalsIgnoreCase(p.getLoaiPhieu())) {

                    // 15.00 => 15%
                    BigDecimal percent = p.getGiaTriGiam()
                            .divide(BigDecimal.valueOf(100));

                    BigDecimal giam = tongTien.multiply(percent);

                    // có giới hạn tối đa
                    if (p.getGiaTriGiamToiDa() != null
                            && p.getGiaTriGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                            && giam.compareTo(p.getGiaTriGiamToiDa()) > 0) {
                        giam = p.getGiaTriGiamToiDa();
                    }

                    tienGiam = tienGiam.add(giam);

                } else if ("TienMat".equalsIgnoreCase(p.getLoaiPhieu())) {

                    tienGiam = tienGiam.add(p.getGiaTriGiam());
                }
            }
        }

        // không cho âm tiền
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }

        hd.setTienGiamGia(tienGiam);
        hd.setTongTienSauGiam(tongTien.subtract(tienGiam));

        hoaDonRepo.save(hd);

        // 4. ✅ TẠO LỊCH SỬ THANH TOÁN (DUY NHẤT 1 BẢN GHI)
        LichSuThanhToan ls = new LichSuThanhToan();
        ls.setHoaDon(hd);
        ls.setSoTien(hd.getTongTienSauGiam());
        ls.setHinhThucThanhToan(
                req.getHinhThucThanhToan() != null
                        ? req.getHinhThucThanhToan()
                        : "TIEN_MAT");
        ls.setNgayThanhToan(LocalDateTime.now());
        ls.setTrangThai(1);

        lichSuThanhToanRepo.save(ls);
    }

    @Override
    public byte[] exportExcel(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to) {
        // 1. Lấy danh sách hóa đơn (Không phân trang để xuất hết)
        // Lưu ý: Bạn cần viết thêm hàm searchOrdersNoPage trong Repo hoặc dùng list
        // findAll có điều kiện
        // Ở đây mình ví dụ lấy tạm tất cả để demo, bạn nên dùng hàm search giống hệt
        // hàm getOrders nhưng bỏ Pageable
        List<HoaDon> list = hoaDonRepo.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

            // 2. Tạo Header (Dòng tiêu đề)
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

            // 3. Đổ dữ liệu vào dòng
            int rowIdx = 1;
            for (HoaDon hd : list) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(hd.getMaHoaDon());
                row.createCell(2).setCellValue(hd.getTenNguoiNhan() != null ? hd.getTenNguoiNhan() : "Khách lẻ");
                row.createCell(3).setCellValue(hd.getNgayTao().toString());
                row.createCell(4).setCellValue(convertTypeToDisplay(hd.getLoaiHoaDon()));
                row.createCell(5).setCellValue(convertStatusToText(hd.getTrangThai())); // Bạn tự viết hàm convert int
                // -> String nhé
                row.createCell(6).setCellValue(hd.getTongTienSauGiam().doubleValue());
            }

            // Auto size cột cho đẹp
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất Excel: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createOrderOnline(CreateOrderRequest req) {

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setTrangThai(1);
        hd.setLoaiHoaDon("TRUC_TUYEN");

        hd.setTenNguoiNhan(
                req.getTenKhachHang() != null ? req.getTenKhachHang() : "Khách lẻ");
        hd.setSdtNguoiNhan(req.getSoDienThoai());
        hd.setDiaChiNguoiNhan(req.getDiaChi());
        hd.setEmailKhachHang(req.getEmail());
        hd.setGhiChu(req.getGhiChu());

        // fallback từ khách hàng (nếu có)
        if (hd.getEmailKhachHang() == null && hd.getKhachHang() != null) {
            hd.setEmailKhachHang(hd.getKhachHang().getEmail());
        }
        // ✅ GÁN NHÂN VIÊN THEO MÃ
        if (req.getMaNhanVien() != null && !req.getMaNhanVien().isBlank()) {
            hd.setNhanVien(
                    nhanVienRepo.findByMaNhanVien(req.getMaNhanVien())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        hoaDonRepo.save(hd);

        BigDecimal tongTien = BigDecimal.ZERO;

        for (CreateOrderRequest.CartItem item : req.getItems()) {
            ChiTietSanPham sp = chiTietSanPhamRepo.findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            if (sp.getSoLuong() < item.getSoLuong()) {
                throw new RuntimeException("Không đủ tồn kho");
            }

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setChiTietSanPham(sp);
            ct.setSoLuong(item.getSoLuong());
            ct.setDonGia(BigDecimal.valueOf(item.getDonGia()));

            BigDecimal thanhTien = ct.getDonGia()
                    .multiply(BigDecimal.valueOf(item.getSoLuong()));
            ct.setThanhTien(thanhTien);

            tongTien = tongTien.add(thanhTien);
            hoaDonChiTietRepo.save(ct);
        }

        hd.setTongTien(tongTien);
        hd.setTienGiamGia(req.getTienGiamGia());
        hd.setTongTienSauGiam(tongTien.subtract(req.getTienGiamGia()));

        hoaDonRepo.save(hd);
    }

    private String convertStatusToText(Integer status) {
        if (status == 1)
            return "Chờ xác nhận";
        if (status == 4)
            return "Hoàn thành";
        return "Khác";
    }

}
