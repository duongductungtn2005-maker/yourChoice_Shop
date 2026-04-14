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
import org.example.yourchoiceshop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // NHỚ THÊM IMPORT NÀY

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private EmailService emailService;
    // 4. Hàm Bật/Tắt trạng thái (Có logic gia hạn)
    public void toggleStatus(Integer id, Map<String, Object> payload) {
        PhieuGiamGia pgg = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        LocalDateTime now = LocalDateTime.now();

        // Lấy cờ sendEmail từ Frontend gửi lên
        boolean isSendEmail = payload.containsKey("sendEmail") && (Boolean) payload.get("sendEmail");

        if (pgg.getTrangThai() == 1) {
            // ĐANG BẬT -> TẮT
            pgg.setTrangThai(0);
            
            // LOGIC GỬI MAIL KHI TẮT PHIẾU CÁ NHÂN
            if (isSendEmail && "CaNhan".equals(pgg.getKieu())) {
                // Lấy danh sách khách hàng sở hữu phiếu này (Dựa vào Repo của m)
                List<PhieuGiamGiaCaNhan> listKhachHang = pggCaNhanRepo.findByPhieuGiamGiaId(id);
                for (PhieuGiamGiaCaNhan pggCn : listKhachHang) {
                    KhachHang kh = pggCn.getKhachHang();
                    if (kh != null && kh.getEmail() != null) {
                        // Gọi hàm gửi mail vừa tạo ở Bước 2
                        emailService.sendVoucherDeactivatedEmail(
                            kh.getEmail(), 
                            kh.getTenKhachHang(), 
                            pgg.getTenPhieuGiamGia()
                        );
                    }
                }
            }
        } else {
            // ĐANG TẮT -> BẬT LẠI (Logic cũ của m giữ nguyên)
            if (pgg.getNgayKetThuc().isBefore(now)) {
                if ("CongKhai".equals(pgg.getKieu())) {
                    String newEndDateStr = (String) payload.get("newEndDate");
                    if (newEndDateStr == null) {
                        throw new RuntimeException("Voucher đã hết hạn. Vui lòng nhập ngày kết thúc mới!");
                    }
                    pgg.setNgayKetThuc(LocalDateTime.parse(newEndDateStr));
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
    public BigDecimal calculateDiscount(String maCode, Integer idKhachHang, BigDecimal tongTienDonHang, BigDecimal phiVanChuyen) {
        
        // 1. Kiểm tra tồn tại
        PhieuGiamGia v = repository.findFirstByMaPhieuGiamGia(maCode)
                .orElseThrow(() -> new RuntimeException("Mã phiếu giảm giá không tồn tại!"));

        // 2. Kiểm tra trạng thái và Hạn sử dụng
        LocalDateTime now = LocalDateTime.now();
        if (v.getTrangThai() == 0 || now.isBefore(v.getNgayBatDau()) || now.isAfter(v.getNgayKetThuc())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn hoặc chưa đến thời gian sử dụng!");
        }

        // 3. Kiểm tra số lượng tổng (Nếu không phải Vô hạn)
        if (v.getSoLuong() != null && v.getSoLuong() <= 0) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        // 4. Kiểm tra điều kiện đơn hàng tối thiểu
        if (v.getDonHangToiThieu() != null && tongTienDonHang.compareTo(v.getDonHangToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt mức tối thiểu " + v.getDonHangToiThieu() + "đ để áp dụng mã này!");
        }

        // 5. Kiểm tra phân loại Cá Nhân / Công Khai
        if ("CaNhan".equals(v.getKieu())) {
            // Nếu là phiếu cá nhân, phải kiểm tra xem khách hàng này có được tặng không
            List<PhieuGiamGiaCaNhan> listCaNhan = pggCaNhanRepo.findByPhieuGiamGiaId(v.getId());
            
            PhieuGiamGiaCaNhan pggCn = listCaNhan.stream()
                    .filter(p -> p.getKhachHang().getId().equals(idKhachHang))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bạn không có quyền sử dụng mã giảm giá này!"));

            // Kiểm tra xem khách đã dùng phiếu này chưa
            if (pggCn.getDaSuDung() != null && pggCn.getDaSuDung()) {
                throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi!");
            }
        } else {
            // NẾU LÀ PHIẾU CÔNG KHAI: 
            // Cần check bảng HoaDon xem Khách hàng này đã dùng mã này mấy lần rồi.
            // (Mày cần tạo hàm đếm trong HoaDonRepository, tao ví dụ logic ở đây)
            // int soLanDaDung = hoaDonRepo.countByKhachHangIdAndPhieuGiamGiaId(idKhachHang, v.getId());
            // int gioiHan = (v.getGioiHanMoiKhach() != null) ? v.getGioiHanMoiKhach() : 1; // Mặc định 1 lần
            // if (soLanDaDung >= gioiHan) {
            //    throw new RuntimeException("Bạn đã hết lượt sử dụng mã này!");
            // }
        }

        // 6. TÍNH TOÁN SỐ TIỀN ĐƯỢC GIẢM
        BigDecimal discountAmount = BigDecimal.ZERO;

        if ("FreeShip".equals(v.getLoaiPhieu())) {
            // Giảm tiền ship (tối đa bằng phí vận chuyển hoặc giá trị giảm tối đa của phiếu)
            discountAmount = phiVanChuyen;
            if (v.getGiaTriGiamToiDa() != null && discountAmount.compareTo(v.getGiaTriGiamToiDa()) > 0) {
                discountAmount = v.getGiaTriGiamToiDa();
            }

        } else if ("TienMat".equals(v.getLoaiPhieu())) {
            // Giảm thẳng tiền mặt
            discountAmount = v.getGiaTriGiam();
            // Không được giảm âm tiền đơn hàng
            if (discountAmount.compareTo(tongTienDonHang) > 0) {
                discountAmount = tongTienDonHang;
            }

        } else if ("PhanTram".equals(v.getLoaiPhieu())) {
            // Giảm theo %: (tongTien * %)/100
            discountAmount = tongTienDonHang.multiply(v.getGiaTriGiam())
                    .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
            
            // Chặn mức giảm tối đa
            if (v.getGiaTriGiamToiDa() != null && discountAmount.compareTo(v.getGiaTriGiamToiDa()) > 0) {
                discountAmount = v.getGiaTriGiamToiDa();
            }
        }

        return discountAmount; // Trả về số tiền được giảm
    }
}