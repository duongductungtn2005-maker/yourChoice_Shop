package org.example.yourchoiceshop.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.repository.HoaDonRepository;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VnPayController {

    private final VnPayService vnPayService;
    private final HoaDonRepository hoaDonRepo;
    private final LichSuThanhToanRepository lichSuThanhToanRepo;

    /**
     * Tạo URL thanh toán VNPay cho hóa đơn đã tồn tại
     */
    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            @RequestParam String maHoaDon,
            @RequestParam long amount,
            HttpServletRequest request) {

        String ipAddress = vnPayService.getIpAddress(request);
        String orderInfo = "Thanh toan don hang " + maHoaDon;
        String paymentUrl = vnPayService.createPaymentUrl(maHoaDon, amount, orderInfo, ipAddress);

        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    /**
     * Nhận kết quả thanh toán từ VNPay (IPN - server-to-server callback)
     * VNPay gọi URL này để thông báo kết quả.
     */
    @GetMapping("/ipn")
    public ResponseEntity<?> vnpayIpn(@RequestParam Map<String, String> params) {
        boolean validSignature = vnPayService.validateSignature(params);
        if (!validSignature) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid Checksum"));
        }

        String maHoaDon = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        var optHd = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (optHd.isEmpty()) {
            return ResponseEntity.ok(Map.of("RspCode", "01", "Message", "Order not found"));
        }

        HoaDon hd = optHd.get();

        // Kiểm tra đã thanh toán chưa
        if (hd.getHinhThucThanhToan() != null && hd.getHinhThucThanhToan().equals("VNPAY")
                && hd.getNgayThanhToan() != null) {
            return ResponseEntity.ok(Map.of("RspCode", "02", "Message", "Order already confirmed"));
        }

        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            hd.setHinhThucThanhToan("VNPAY");
            hd.setNgayThanhToan(LocalDateTime.now());
            hoaDonRepo.save(hd);

            // Ghi lịch sử thanh toán
            LichSuThanhToan ls = new LichSuThanhToan();
            ls.setHoaDon(hd);
            ls.setMaGiaoDich(transactionNo);
            ls.setSoTien(new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
            ls.setNgayThanhToan(LocalDateTime.now());
            ls.setHinhThucThanhToan("VNPAY");
            ls.setLoaiThanhToan("CHUYEN_KHOAN");
            ls.setGhiChu("Thanh toán qua VNPay");
            ls.setTrangThai(1);
            lichSuThanhToanRepo.save(ls);

            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        }
    }

    /**
     * Frontend gọi API này để kiểm tra kết quả thanh toán sau khi VNPay redirect về
     */
    @GetMapping("/payment-result")
    public ResponseEntity<?> paymentResult(@RequestParam Map<String, String> params) {
        boolean validSignature = vnPayService.validateSignature(params);

        String maHoaDon = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        if (!validSignature) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Chữ ký không hợp lệ",
                    "maHoaDon", maHoaDon != null ? maHoaDon : ""
            ));
        }

        if (!"00".equals(responseCode)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Thanh toán thất bại. Mã lỗi: " + responseCode,
                    "maHoaDon", maHoaDon != null ? maHoaDon : ""
            ));
        }

        // Thanh toán thành công - cập nhật hóa đơn
        var optHd = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (optHd.isPresent()) {
            HoaDon hd = optHd.get();

            // Chỉ cập nhật nếu chưa được xử lý bởi IPN
            if (hd.getNgayThanhToan() == null) {
                hd.setHinhThucThanhToan("VNPAY");
                hd.setNgayThanhToan(LocalDateTime.now());
                hoaDonRepo.save(hd);

                LichSuThanhToan ls = new LichSuThanhToan();
                ls.setHoaDon(hd);
                ls.setMaGiaoDich(transactionNo);
                ls.setSoTien(new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
                ls.setNgayThanhToan(LocalDateTime.now());
                ls.setHinhThucThanhToan("VNPAY");
                ls.setLoaiThanhToan("CHUYEN_KHOAN");
                ls.setGhiChu("Thanh toán qua VNPay");
                ls.setTrangThai(1);
                lichSuThanhToanRepo.save(ls);
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thanh toán thành công",
                "maHoaDon", maHoaDon,
                "transactionNo", transactionNo != null ? transactionNo : ""
        ));
    }
}
