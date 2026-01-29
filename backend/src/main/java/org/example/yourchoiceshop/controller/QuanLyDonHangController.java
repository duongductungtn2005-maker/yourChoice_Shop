package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
// import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
// import org.example.yourchoiceshop.entity.HoaDon;
// import org.example.yourchoiceshop.service.ChiTietDonHangService;
import org.example.yourchoiceshop.service.HoaDonService;
import org.example.yourchoiceshop.service.ChiTietDonHangService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class QuanLyDonHangController {

    private final HoaDonService hoaDonService;
    private final ChiTietDonHangService chiTietDonHangService;

    @PostMapping
    public Page<QuanLyDonHangResponse> searchOrders(
            @RequestBody QuanLyDonHangRequest request) {

        return hoaDonService.searchDonHang(request);
    }
//    private final HoaDonService hoaDonService;

@GetMapping("/{id}")
public ChiTietDonHangResponse getOrderDetail(@PathVariable Long id) {
    return chiTietDonHangService.getChiTietDonHang(id);
}



}