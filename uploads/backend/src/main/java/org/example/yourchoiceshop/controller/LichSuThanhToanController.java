package org.example.yourchoiceshop.controller;

import java.util.List;

import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.service.LichSuThanhToanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lich-su-thanh-toan")
@RequiredArgsConstructor
public class LichSuThanhToanController {

    private final LichSuThanhToanService service;

    @GetMapping("/hoa-don/{hoaDonId}")
    public List<LichSuThanhToan> getByHoaDon(
            @PathVariable Integer hoaDonId) {
        return service.getByHoaDonId(hoaDonId);
    }
}