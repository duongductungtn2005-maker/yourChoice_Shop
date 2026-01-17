package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    @Override
    public Page<QuanLyDonHangResponse> getDanhSachDonHang(QuanLyDonHangRequest request) {
        return Page.empty();
    }
}
