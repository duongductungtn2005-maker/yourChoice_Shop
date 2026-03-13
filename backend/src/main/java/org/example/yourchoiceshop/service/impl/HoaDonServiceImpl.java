package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.dto.response.LichSuHoaDonResponse;
import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.HoaDonChiTiet;
import org.example.yourchoiceshop.entity.LichSuHoaDon;
import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.example.yourchoiceshop.repository.ChiTietSanPhamRepository;
import org.example.yourchoiceshop.repository.HoaDonChiTietRepository;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuHoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.example.yourchoiceshop.repository.PhieuGiamGiaRepository;
import org.example.yourchoiceshop.service.HoaDonService; // <--- Import Interface này
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private static final int POS_DRAFT_STATUS = 9;

    private final HoaDonRepository hoaDonRepo;
    private final HoaDonChiTietRepository hoaDonChiTietRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final NhanVienRepository nhanVienRepo; // Thêm Repository này để lấy tên nhân viên
    private final LichSuThanhToanRepository lichSuThanhToanRepo; // Thêm Repository này để lấy lịch sử thanh toán
    private final LichSuHoaDonRepository lichSuHoaDonRepo;
    private final PhieuGiamGiaRepository phieuGiamGiaRepo;

    @Override // <--- Thêm Override cho chắc chắn
    public Page<HoaDonResponse> getOrders(String keyword, Integer status, String type, LocalDateTime from,
            LocalDateTime to, Pageable pageable) {
        Page<HoaDon> page = hoaDonRepo.searchOrders(keyword, status, type, from, to, pageable);

        return page.map(hd -> {
            HoaDonResponse res = new HoaDonResponse();
            res.setMaHoaDon(hd.getMaHoaDon());
            res.setTongSanPham(0);

            res.setTongTienSauGiam(hd.getTongTienSauGiam());

            res.setTenKhachHang(
                    hd.getTenNguoiNhan() != null
                            ? hd.getTenNguoiNhan()
                            : (hd.getKhachHang() != null ? hd.getKhachHang().getTenKhachHang() : "Khách lẻ"));

            res.setSdtKhachHang(
                    hd.getSdtNguoiNhan() != null
                            ? hd.getSdtNguoiNhan()
                            : (hd.getKhachHang() != null ? hd.getKhachHang().getSoDienThoai() : null));

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
        res.setGhiChu(hd.getGhiChu());
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
                p.setMaSanPham(ct.getChiTietSanPham().getSanPham().getMaSanPham());
                p.setTenSanPham(ct.getChiTietSanPham().getSanPham().getTenSanPham());
                p.setThuongHieu(
                    ct.getChiTietSanPham().getThuongHieu() != null
                        ? ct.getChiTietSanPham().getThuongHieu().getTenThuongHieu()
                        : (ct.getChiTietSanPham().getSanPham().getThuongHieu() != null
                            ? ct.getChiTietSanPham().getSanPham().getThuongHieu().getTenThuongHieu()
                            : null));
                p.setChatLieu(
                    ct.getChiTietSanPham().getChatLieu() != null
                        ? ct.getChiTietSanPham().getChatLieu().getTenChatLieu()
                        : (ct.getChiTietSanPham().getSanPham().getChatLieu() != null
                            ? ct.getChiTietSanPham().getSanPham().getChatLieu().getTenChatLieu()
                            : null));
                p.setSize(
                    ct.getChiTietSanPham().getKichThuoc() != null
                        ? ct.getChiTietSanPham().getKichThuoc().getTenKichThuoc()
                        : null);
                p.setMauSac(
                    ct.getChiTietSanPham().getMauSac() != null
                        ? ct.getChiTietSanPham().getMauSac().getTenMauSac()
                        : null);
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

        List<LichSuHoaDonResponse> histories = new ArrayList<>();
        if (hd.getLichSuHoaDons() != null) {
            histories = hd.getLichSuHoaDons().stream()
                    .map(LichSuHoaDonResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        res.setLichSuHoaDon(histories);

        return res;
    }

    private String convertTypeToDisplay(String dbType) {
        if (dbType == null) return "Không xác định";
        String type = dbType.trim().toUpperCase();
        if (type.equals("TRUC_TUYEN")) return "Trực tuyến";
        if (type.equals("TAI_QUAY")) return "Tại quầy";
        if (type.equals("GIAO_HANG")) return "Giao hàng";
        return "Không xác định";
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

        // 2. Lưu trạng thái cũ để tạo lịch sử
        Integer oldStatus = hd.getTrangThai();

        // 3. Nếu hủy đơn (stock đã trừ khi tạo đơn → cần hoàn kho)
        if (Integer.valueOf(0).equals(newStatus) && oldStatus != null && oldStatus >= 1) {
            restoreStockForCanceledOrder(hd);
        }

        // 4. Sau khi releaseStock clear context, cần re-fetch hóa đơn
        hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));
        
        // 5. Cập nhật trạng thái mới
        hd.setTrangThai(newStatus);

        // 6. Lưu hóa đơn vào Database
        hoaDonRepo.save(hd);
        
        // 7. Tạo lịch sử thay đổi trạng thái
        LichSuHoaDon history = new LichSuHoaDon();
        history.setHoaDon(hd);
        history.setHanhDong("Cập nhật trạng thái từ " + getStatusLabel(oldStatus) + " sang " + getStatusLabel(newStatus));
        history.setThoiGian(LocalDateTime.now());
        history.setTrangThai(newStatus);
        history.setGhiChu("Thay đổi tự động từ hệ thống");
        
        lichSuHoaDonRepo.save(history);
    }

    private void restoreStockForCanceledOrder(HoaDon hd) {
        if (hd.getHoaDonChiTiets() == null || hd.getHoaDonChiTiets().isEmpty()) {
            return;
        }

        // Thu thập dữ liệu trước để tránh LazyInitializationException
        List<Object[]> items = new ArrayList<>();
        for (HoaDonChiTiet item : hd.getHoaDonChiTiets()) {
            if (item == null || item.getChiTietSanPham() == null || item.getChiTietSanPham().getId() == null) {
                continue;
            }
            Integer soLuong = item.getSoLuong();
            if (soLuong == null || soLuong <= 0) {
                continue;
            }
            items.add(new Object[]{item.getChiTietSanPham().getId(), soLuong});
        }

        for (Object[] data : items) {
            chiTietSanPhamRepo.releaseStock((Integer) data[0], (Integer) data[1]);
        }
    }

    private void deductStockForConfirmedOrder(HoaDon hd) {
        if (hd.getHoaDonChiTiets() == null || hd.getHoaDonChiTiets().isEmpty()) {
            return;
        }

        // Thu thập dữ liệu trước để tránh LazyInitializationException
        // (reserveStock có clearAutomatically=true sẽ xóa persistence context)
        List<Object[]> items = new ArrayList<>();
        for (HoaDonChiTiet item : hd.getHoaDonChiTiets()) {
            if (item == null || item.getChiTietSanPham() == null || item.getChiTietSanPham().getId() == null) {
                continue;
            }
            Integer soLuong = item.getSoLuong();
            if (soLuong == null || soLuong <= 0) {
                continue;
            }
            items.add(new Object[]{
                    item.getChiTietSanPham().getId(),
                    soLuong,
                    item.getChiTietSanPham().getMaCtsp()
            });
        }

        for (Object[] data : items) {
            Integer id = (Integer) data[0];
            Integer soLuong = (Integer) data[1];
            String maCtsp = (String) data[2];

            int reduced = chiTietSanPhamRepo.reserveStock(id, soLuong);
            if (reduced == 0) {
                throw new RuntimeException("Sản phẩm " + maCtsp
                        + " không đủ số lượng trong kho để xác nhận đơn hàng");
            }
        }
    }

    private void cancelUnfulfillablePendingOrders(Set<Integer> affectedProductIds) {
        if (affectedProductIds == null || affectedProductIds.isEmpty()) return;

        List<HoaDon> pendingOrders = hoaDonRepo.findPendingOrdersByProductIds(affectedProductIds);

        for (HoaDon pending : pendingOrders) {
            boolean canFulfill = true;
            for (HoaDonChiTiet item : pending.getHoaDonChiTiets()) {
                if (item.getChiTietSanPham() == null) continue;
                if (item.getChiTietSanPham().getSoLuong() == null
                        || item.getChiTietSanPham().getSoLuong() < item.getSoLuong()) {
                    canFulfill = false;
                    break;
                }
            }

            if (!canFulfill) {
                pending.setTrangThai(0);
                hoaDonRepo.save(pending);

                LichSuHoaDon history = new LichSuHoaDon();
                history.setHoaDon(pending);
                history.setHanhDong("Đơn hàng đã bị hủy tự động");
                history.setThoiGian(LocalDateTime.now());
                history.setTrangThai(0);
                history.setGhiChu("Đã hủy do không đủ số lượng sản phẩm");
                lichSuHoaDonRepo.save(history);
            }
        }
    }

    private String getStatusLabel(Integer status) {
        if (status == null) return "Không xác định";
        return switch(status) {
            case 0 -> "Đã hủy";
            case 1 -> "Chờ xác nhận";
            case 2 -> "Chờ giao hàng";
            case 3 -> "Đang vận chuyển";
            case 4 -> "Chờ thanh toán";
            case 5 -> "Hoàn thành";
            default -> "Không xác định";
        };
    }

    private BigDecimal calculateAndConsumeVoucherDiscount(List<PhieuGiamGiaRequest> vouchers, BigDecimal orderTotal) {
        if (vouchers == null || vouchers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        Set<Integer> consumedVoucherIds = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (PhieuGiamGiaRequest reqVoucher : vouchers) {
            // Hỗ trợ tìm bằng id hoặc maPhieuGiamGia (online checkout gửi mã, không gửi id)
            PhieuGiamGia voucher = null;
            Integer lookupId = reqVoucher.getId();

            if (lookupId != null) {
                if (consumedVoucherIds.contains(lookupId)) continue;
                voucher = phieuGiamGiaRepo.findById(lookupId).orElse(null);
            } else if (reqVoucher.getMaPhieuGiamGia() != null && !reqVoucher.getMaPhieuGiamGia().isBlank()) {
                voucher = phieuGiamGiaRepo.findFirstByMaPhieuGiamGia(reqVoucher.getMaPhieuGiamGia()).orElse(null);
                if (voucher != null && consumedVoucherIds.contains(voucher.getId())) continue;
            } else {
                continue;
            }
            if (voucher == null) continue;
            if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1) continue;
            if (voucher.getSoLuong() == null || voucher.getSoLuong() <= 0) continue;

            if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) continue;
            if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) continue;
            if (voucher.getDonHangToiThieu() != null && orderTotal.compareTo(voucher.getDonHangToiThieu()) < 0) continue;

            BigDecimal discount = BigDecimal.ZERO;

            if ("PhanTram".equalsIgnoreCase(voucher.getLoaiPhieu())) {
                BigDecimal percent = voucher.getGiaTriGiam().divide(BigDecimal.valueOf(100));
                discount = orderTotal.multiply(percent);

                if (voucher.getGiaTriGiamToiDa() != null
                        && voucher.getGiaTriGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && discount.compareTo(voucher.getGiaTriGiamToiDa()) > 0) {
                    discount = voucher.getGiaTriGiamToiDa();
                }
            } else if ("TienMat".equalsIgnoreCase(voucher.getLoaiPhieu())) {
                discount = voucher.getGiaTriGiam();
            }

            totalDiscount = totalDiscount.add(discount);

            voucher.setSoLuong(voucher.getSoLuong() - 1);
            if (voucher.getSoLuong() <= 0) {
                voucher.setSoLuong(0);
                voucher.setTrangThai(0);
            }
            phieuGiamGiaRepo.save(voucher);
            consumedVoucherIds.add(voucher.getId());
        }

        return totalDiscount;
    }

    @Override
    @Transactional
    public String createDraftOrderAtCounter(Integer idNhanVien) {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setLoaiHoaDon("TAI_QUAY");
        hd.setTrangThai(POS_DRAFT_STATUS);

        hd.setTongTien(BigDecimal.ZERO);
        hd.setTienGiamGia(BigDecimal.ZERO);
        hd.setPhiVanChuyen(BigDecimal.ZERO);
        hd.setTongTienSauGiam(BigDecimal.ZERO);

        if (idNhanVien != null) {
            hd.setNhanVien(
                    nhanVienRepo.findById(idNhanVien)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        hoaDonRepo.save(hd);
        return hd.getMaHoaDon();
    }

    @Override
    @Transactional
    public void deleteDraftOrderAtCounter(String maHoaDon) {
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + maHoaDon));

        if (!"TAI_QUAY".equalsIgnoreCase(hd.getLoaiHoaDon()) || hd.getTrangThai() == null || hd.getTrangThai() != POS_DRAFT_STATUS) {
            throw new RuntimeException("Chỉ có thể xóa hóa đơn nháp tại quầy");
        }

        hoaDonRepo.delete(hd);
    }

    @Override
    @Transactional
    public void createOrderAtCounter(CreateOrderRequest req) {

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        // 1. Lấy hóa đơn nháp (nếu có), nếu không thì tạo mới để tương thích ngược.
        HoaDon hd;
        if (req.getMaHoaDon() != null && !req.getMaHoaDon().isBlank()) {
            hd = hoaDonRepo.findByMaHoaDon(req.getMaHoaDon())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn: " + req.getMaHoaDon()));

            if (!"TAI_QUAY".equalsIgnoreCase(hd.getLoaiHoaDon()) || hd.getTrangThai() == null || hd.getTrangThai() != POS_DRAFT_STATUS) {
                throw new RuntimeException("Hóa đơn không còn ở trạng thái nháp để thanh toán");
            }
        } else {
            hd = new HoaDon();
            hd.setMaHoaDon("HD" + System.currentTimeMillis());
            hd.setNgayTao(LocalDateTime.now());
            hd.setLoaiHoaDon("TAI_QUAY");
        }

        hd.setTrangThai(5);
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setHinhThucThanhToan(
                req.getHinhThucThanhToan() != null
                        ? req.getHinhThucThanhToan()
                        : "TIEN_MAT");

        hd.setTenNguoiNhan(
                req.getTenKhachHang() != null ? req.getTenKhachHang() : "Khách lẻ");
        hd.setSdtNguoiNhan(req.getSoDienThoai());
        hd.setDiaChiNguoiNhan(buildFullAddress(req));
        hd.setEmailKhachHang(req.getEmail());
        hd.setGhiChu(req.getGhiChu());

        // fallback từ khách hàng (nếu có)
        if (hd.getEmailKhachHang() == null && hd.getKhachHang() != null) {
            hd.setEmailKhachHang(hd.getKhachHang().getEmail());
        }
        // ✅ GÁN NHÂN VIÊN THEO MÃ
        if (req.getIdNhanVien() != null) {
            hd.setNhanVien(
                    nhanVienRepo.findById(req.getIdNhanVien())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        hoaDonRepo.save(hd);

        // 2. Lưu chi tiết hóa đơn
        BigDecimal tongTien = BigDecimal.ZERO;

        for (CreateOrderRequest.CartItem item : req.getItems()) {

            ChiTietSanPham sp = chiTietSanPhamRepo
                    .findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

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

        BigDecimal tienGiam = calculateAndConsumeVoucherDiscount(req.getPhieuGiamGia(), tongTien);

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
        ls.setHinhThucThanhToan(hd.getHinhThucThanhToan());
        ls.setNgayThanhToan(LocalDateTime.now());
        ls.setTrangThai(1);

        lichSuThanhToanRepo.save(ls);
        
        // 5. ✅ TẠO LỊCH SỬ HÓA ĐƠN (Ghi nhận tạo đơn hàng)
        LichSuHoaDon history = new LichSuHoaDon();
        history.setHoaDon(hd);
        history.setHanhDong("Thanh toán đơn hàng");
        history.setThoiGian(LocalDateTime.now());
        history.setTrangThai(hd.getTrangThai());
        history.setGhiChu("Đơn hàng được thanh toán tại quầy");
        if (req.getIdNhanVien() != null) {
            history.setNhanVien(hd.getNhanVien());
        }
        
        lichSuHoaDonRepo.save(history);
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
    public String createOrderDelivery(CreateOrderRequest req) {

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setTrangThai(1);
        hd.setLoaiHoaDon("GIAO_HANG");

        hd.setTenNguoiNhan(
                req.getTenKhachHang() != null ? req.getTenKhachHang() : "Khách lẻ");
        hd.setSdtNguoiNhan(req.getSoDienThoai());
        hd.setDiaChiNguoiNhan(buildFullAddress(req));
        hd.setEmailKhachHang(req.getEmail());
        hd.setGhiChu(req.getGhiChu());

        // fallback từ khách hàng (nếu có)
        if (hd.getEmailKhachHang() == null && hd.getKhachHang() != null) {
            hd.setEmailKhachHang(hd.getKhachHang().getEmail());
        }
        // ✅ GÁN NHÂN VIÊN THEO MÃ
        if (req.getIdNhanVien() != null) {
            hd.setNhanVien(
                    nhanVienRepo.findById(req.getIdNhanVien())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        hoaDonRepo.save(hd);

        BigDecimal tongTien = BigDecimal.ZERO;

        // Thu thập thông tin items trước khi reserveStock (tránh LazyInitializationException)
        List<Object[]> itemsToReserve = new ArrayList<>();
        for (CreateOrderRequest.CartItem item : req.getItems()) {
            ChiTietSanPham sp = chiTietSanPhamRepo.findById(item.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            itemsToReserve.add(new Object[]{sp, item});
        }

        // Trừ tồn kho ngay khi đặt hàng (atomic) để tránh race condition
        for (Object[] data : itemsToReserve) {
            ChiTietSanPham sp = (ChiTietSanPham) data[0];
            CreateOrderRequest.CartItem item = (CreateOrderRequest.CartItem) data[1];
            int reserved = chiTietSanPhamRepo.reserveStock(sp.getId(), item.getSoLuong());
            if (reserved == 0) {
                throw new RuntimeException("Sản phẩm " + sp.getMaCtsp()
                        + " không đủ số lượng trong kho (còn lại: " + sp.getSoLuong() + ", cần: " + item.getSoLuong() + ")");
            }
        }

        // Re-fetch hóa đơn sau reserveStock (clearAutomatically=true xóa context)
        hd = hoaDonRepo.findByMaHoaDon(hd.getMaHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        for (Object[] data : itemsToReserve) {
            ChiTietSanPham sp = (ChiTietSanPham) data[0];
            CreateOrderRequest.CartItem item = (CreateOrderRequest.CartItem) data[1];

            // Re-fetch sp sau khi context bị clear
            sp = chiTietSanPhamRepo.findById(sp.getId()).orElse(sp);

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

        BigDecimal phiVanChuyen = req.getPhiVanChuyen() != null
                ? req.getPhiVanChuyen()
                : BigDecimal.ZERO;

        hd.setPhiVanChuyen(phiVanChuyen);

        hd.setTongTien(tongTien);
        BigDecimal tienGiam = calculateAndConsumeVoucherDiscount(req.getPhieuGiamGia(), tongTien);
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }
        hd.setTienGiamGia(tienGiam);

        hd.setTongTienSauGiam(
                tongTien
                .subtract(tienGiam)
                        .add(phiVanChuyen));

        hoaDonRepo.save(hd);
        
        // ✅ TẠO LỊCH SỬ HÓA ĐƠN (Ghi nhận tạo đơn hàng online)
        LichSuHoaDon history = new LichSuHoaDon();
        history.setHoaDon(hd);
        history.setHanhDong("Tạo đơn hàng mới");
        history.setThoiGian(LocalDateTime.now());
        history.setTrangThai(hd.getTrangThai());
        history.setGhiChu("Đơn hàng được tạo trực tuyến");
        if (req.getIdNhanVien() != null) {
            history.setNhanVien(hd.getNhanVien());
        }
        
        lichSuHoaDonRepo.save(history);

        return hd.getMaHoaDon();
    }

    private String convertStatusToText(Integer status) {
        if (status == 1)
            return "Chờ xác nhận";
        if (status == 4)
            return "Hoàn thành";
        return "Khác";
    }

    private String buildFullAddress(CreateOrderRequest req) {

    String detail = req.getDiaChiChiTiet();
    String ward = req.getWardName();
    String district = req.getDistrictName();
    String province = req.getProvinceName();

    StringBuilder sb = new StringBuilder();

    if (detail != null && !detail.isBlank()) {
        sb.append(detail);
    }

    if (ward != null && !ward.isBlank()) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(ward);
    }

    if (district != null && !district.isBlank()) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(district);
    }

    if (province != null && !province.isBlank()) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(province);
    }

    return sb.toString();
}
}
