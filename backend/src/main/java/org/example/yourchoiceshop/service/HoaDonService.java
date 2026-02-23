package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.request.HoaDonRequest;
import org.example.yourchoiceshop.dto.request.PaymentRequest;
import org.example.yourchoiceshop.dto.request.QuanLyDonHangRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface HoaDonService {
    // Hàm cũ bạn đã có
    Page<QuanLyDonHangResponse> getDanhSachDonHang(QuanLyDonHangRequest request);
    Page<QuanLyDonHangResponse> searchDonHang(QuanLyDonHangRequest request); // Thêm dòng này

    // --- CÁC HÀM CẦN THÊM ĐỂ KHỚP CONTROLLER ---
    Page<HoaDonResponse> getOrders(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    HoaDonDetailResponse getOrderDetail(String maHoaDon);

    void updateStatus(String maHoaDon, Integer newStatus);

    void updateOrderInfo(String maHoaDon, HoaDonRequest request);

    void createOrderAtCounter(CreateOrderRequest req);
    void confirmPayment(String maHoaDon, PaymentRequest request);
    byte[] exportExcel(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to);
}