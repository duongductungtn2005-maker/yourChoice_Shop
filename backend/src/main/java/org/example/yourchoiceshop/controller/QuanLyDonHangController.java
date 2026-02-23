package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.service.ChiTietDonHangService;
import org.example.yourchoiceshop.service.HoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.yourchoiceshop.dto.request.PaymentRequest; // Đảm bảo import DTO này
@RestController
@RequestMapping("/api/v1/admin/orders") // ĐẢM BẢO CÓ /api/v1 ĐỂ KHỚP VỚI FE
@RequiredArgsConstructor
@CrossOrigin("*")
public class QuanLyDonHangController {

    private final HoaDonService hoaDonService;
    private final ChiTietDonHangService chiTietDonHangService;
    @PostMapping
    public Page<QuanLyDonHangResponse> searchOrders(@RequestBody QuanLyDonHangRequest request) {
        return hoaDonService.searchDonHang(request);
    }

    // Nhận String maHoaDon để khớp với URL Frontend gửi lên
    @GetMapping("/{maHoaDon}")
    public HoaDonDetailResponse getOrderDetail(@PathVariable String maHoaDon) {
        return hoaDonService.getOrderDetail(maHoaDon);
    }
    @PostMapping("/{maHoaDon}/payment")
    public ResponseEntity<?> confirmPayment(
            @PathVariable String maHoaDon,
            @RequestBody PaymentRequest request
    ) {
        hoaDonService.confirmPayment(maHoaDon, request);
        return ResponseEntity.ok("Thanh toán thành công");
    }
}