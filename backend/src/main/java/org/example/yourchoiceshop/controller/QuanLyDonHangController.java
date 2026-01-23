package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.service.ChiTietDonHangService;
import org.example.yourchoiceshop.service.HoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class QuanLyDonHangController {

    private final HoaDonService hoaDonService;

    @PostMapping
    public Page<QuanLyDonHangResponse> getDanhSachDonHang(@RequestBody QuanLyDonHangRequest request) {
        return hoaDonService.getDanhSachDonHang(request);
    }

    private final ChiTietDonHangService chiTietDonHangService;

    @GetMapping("/{id}")
    public ChiTietDonHangResponse getChiTiet(@PathVariable Integer id) {
        return chiTietDonHangService.getChiTietDonHang(id);
    }
}