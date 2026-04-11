package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.CreateOrderRequest;
import org.example.yourchoiceshop.dto.request.HoaDonRequest; // <--- Import DTO mới
import org.example.yourchoiceshop.dto.request.ThanhToanRequest;
import org.example.yourchoiceshop.dto.response.HoaDonDetailResponse;
import org.example.yourchoiceshop.dto.response.HoaDonResponse;
import org.example.yourchoiceshop.service.HoaDonService;
import org.example.yourchoiceshop.service.ThanhToanService;
import org.example.yourchoiceshop.service.impl.HoaDonServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    // API danh sách
    @GetMapping
    
    public ResponseEntity<Page<HoaDonResponse>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer khachHangId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime to = (toDate != null) ? toDate.atTime(23, 59, 59) : null;
        String typeDb = null;
        if ("Trực tuyến".equals(type))
            typeDb = "TRUC_TUYEN";
        if ("Tại quầy".equals(type))
            typeDb = "TAI_QUAY";
        if ("Giao hàng".equals(type))
            typeDb = "GIAO_HANG";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").ascending());

        return ResponseEntity.ok(hoaDonService.getOrders(keyword, status, typeDb, khachHangId, from, to, pageable));
    }

    // API chi tiết
    @GetMapping("/{maHoaDon}")
    public ResponseEntity<HoaDonDetailResponse> getDetail(@PathVariable String maHoaDon) {
        return ResponseEntity.ok(hoaDonService.getOrderDetail(maHoaDon));
    }

    // API đổi trạng thái (Giao hàng, Hủy...)
    @PutMapping("/{maHoaDon}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String maHoaDon,
            @RequestParam Integer newStatus) {
        try {
            hoaDonService.updateStatus(maHoaDon, newStatus);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- API MỚI: CẬP NHẬT THÔNG TIN NGƯỜI NHẬN ---
    @PutMapping("/{maHoaDon}/info")
    public ResponseEntity<?> updateOrderInfo(
            @PathVariable String maHoaDon,
            @RequestBody HoaDonRequest request) {
        hoaDonService.updateOrderInfo(maHoaDon, request);
        return ResponseEntity.ok().body("Cập nhật thông tin thành công");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) {
        try {
            hoaDonService.createOrderAtCounter(req);
            return ResponseEntity.ok("Tạo hóa đơn thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/pos/draft")
    public ResponseEntity<?> createPosDraft(@RequestBody(required = false) CreateOrderRequest req) {
        Integer idNhanVien = req != null ? req.getIdNhanVien() : null;
        String maHoaDon = hoaDonService.createDraftOrderAtCounter(idNhanVien);
        return ResponseEntity.ok(Map.of("maHoaDon", maHoaDon));
    }

    @DeleteMapping("/pos/draft/{maHoaDon}")
    public ResponseEntity<?> deletePosDraft(@PathVariable String maHoaDon) {
        hoaDonService.deleteDraftOrderAtCounter(maHoaDon);
        return ResponseEntity.ok("Xóa hóa đơn nháp thành công");
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime to = (toDate != null) ? toDate.atTime(23, 59, 59) : null;
        String typeDb = null;
        if ("Trực tuyến".equals(type))
            typeDb = "TRUC_TUYEN";
        if ("Tại quầy".equals(type))
            typeDb = "TAI_QUAY";
        if ("Giao hàng".equals(type))
            typeDb = "GIAO_HANG";

        byte[] excelData = hoaDonService.exportExcel(keyword, status, typeDb, from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DanhSachHoaDon.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    private final ThanhToanService thanhToanService;

    @PostMapping("/{maHoaDon}/payment")
    public ResponseEntity<?> thanhToan(
            @PathVariable String maHoaDon,
            @RequestBody ThanhToanRequest request) {
        try {
            thanhToanService.thanhToan(maHoaDon, request);
            return ResponseEntity.ok("Thanh toán thành công");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/delivery")
    public ResponseEntity<?> createOrderDelivery(
            @RequestBody CreateOrderRequest req) {
        try {
            String maHoaDon = hoaDonService.createOrderDelivery(req);
            return ResponseEntity.ok(java.util.Map.of("maHoaDon", maHoaDon));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/tai-quay")
    public ResponseEntity<?> createOrderAtCounter(
            @RequestBody CreateOrderRequest req) {
        try {
            hoaDonService.createOrderAtCounter(req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Tra cứu đơn hàng theo SĐT (cho khách vãng lai, không cần đăng nhập)
    @GetMapping("/tra-cuu")
    public ResponseEntity<Page<HoaDonResponse>> trackOrderByPhone(
            @RequestParam String soDienThoai,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").descending());
        return ResponseEntity.ok(hoaDonService.getOrdersByPhone(soDienThoai, keyword, status, pageable));
    }

    
}