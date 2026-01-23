package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.springframework.data.domain.Page;

public interface HoaDonService {

    Page<QuanLyDonHangResponse> getDanhSachDonHang(QuanLyDonHangRequest request);
}
