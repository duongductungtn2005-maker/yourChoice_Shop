package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.service.impl.HoaDonService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class QuanLyDonHangController {

    private final HoaDonService hoaDonService;

    @PostMapping
    public Page<QuanLyDonHangResponse> getDanhSachDonHang(
            @RequestBody QuanLyDonHangRequest request
    ) {
        return hoaDonService.getDanhSachDonHang(request);
    }
}
