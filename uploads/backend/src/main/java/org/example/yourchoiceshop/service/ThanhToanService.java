package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.ThanhToanRequest;

public interface ThanhToanService {
    void thanhToan(String maHoaDon, ThanhToanRequest request);
}