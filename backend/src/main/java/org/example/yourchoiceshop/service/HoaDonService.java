package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.request.HoaDonRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface HoaDonService {

    // Hàm lấy danh sách hóa đơn (cho màn Quản lý đơn hàng)
    Page<HoaDonResponse> getOrders(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Hàm lấy chi tiết hóa đơn (cho màn Chi tiết đơn hàng)
    HoaDonDetailResponse getOrderDetail(String maHoaDon);
    void updateStatus(String maHoaDon, Integer newStatus);
    void updateOrderInfo(String maHoaDon, HoaDonRequest request);
    void createOrderAtCounter(CreateOrderRequest req);
    byte[] exportExcel(String keyword, Integer status, String type, LocalDateTime from, LocalDateTime to);
    void createOrderDelivery(CreateOrderRequest req);
}