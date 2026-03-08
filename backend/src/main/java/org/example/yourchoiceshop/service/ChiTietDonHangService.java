package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.response.ChiTietDonHangResponse;

public interface ChiTietDonHangService {
    // Đổi Integer id -> String maHoaDon
    ChiTietDonHangResponse getChiTietDonHang(String maHoaDon);
}