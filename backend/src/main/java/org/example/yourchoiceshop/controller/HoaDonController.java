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

@RestController
@RequestMapping("/api/v1/hoa-don")
@RequiredArgsConstructor
@CrossOrigin("*")
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
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime to = (toDate != null) ? toDate.atTime(23, 59, 59) : null;
        String typeDb = null;
        if ("Trực tuyến".equals(type))
            typeDb = "TRUC_TUYEN";
        if ("Tại quầy".equals(type))
            typeDb = "TAI_QUAY";
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").descending());

        return ResponseEntity.ok(hoaDonService.getOrders(keyword, status, typeDb, from, to, pageable));
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
        hoaDonService.updateStatus(maHoaDon, newStatus);
        return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
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
        hoaDonService.createOrderAtCounter(req);
        return ResponseEntity.ok("Tạo hóa đơn thành công");
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

        thanhToanService.thanhToan(maHoaDon, request);
        return ResponseEntity.ok("Thanh toán thành công");
    }

    @PostMapping("/online")
    public ResponseEntity<?> createOrderOnline(
            @RequestBody CreateOrderRequest req) {

        hoaDonService.createOrderOnline(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tai-quay")
    public ResponseEntity<?> createOrderAtCounter(
            @RequestBody CreateOrderRequest req) {

        hoaDonService.createOrderAtCounter(req);
        return ResponseEntity.ok().build();
    }

    
}