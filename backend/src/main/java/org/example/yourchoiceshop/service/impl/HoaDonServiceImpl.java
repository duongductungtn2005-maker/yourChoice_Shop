// package org.example.yourchoiceshop.service.impl;

// import lombok.RequiredArgsConstructor;
// import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
// import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
// import org.example.yourchoiceshop.repository.HoaDonRepository;
// import org.example.yourchoiceshop.service.HoaDonService;
// import org.springframework.data.domain.Page;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class HoaDonServiceImpl implements HoaDonService {

//     private final HoaDonRepository hoaDonRepository;

// @Override
// public Page<QuanLyDonHangResponse> getDanhSachDonHang(QuanLyDonHangRequest request) {
//     return hoaDonRepository.findForQuanLy(request.toPageable());
// }


// }
