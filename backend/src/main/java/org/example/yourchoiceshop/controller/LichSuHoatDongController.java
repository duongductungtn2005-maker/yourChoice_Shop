package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO;
import org.example.yourchoiceshop.service.GiaoCaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/lich-su-hoat-dong")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LichSuHoatDongController {

    private final GiaoCaService giaoCaService;

    @GetMapping
    public ResponseEntity<Page<LichSuHoatDongDTO>> getLichSu(
            @RequestParam(value = "employee", required = false) String employee,
            @RequestParam(value = "shift", required = false) String tenCa,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "page", defaultValue = "1") int page, // Vue gửi 1 là trang đầu
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty()) ? LocalDate.parse(startDateStr) : null;
        LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) ? LocalDate.parse(endDateStr) : null;

        // Xử lý đồng bộ Vue và Spring Boot: Nếu page > 0 thì trừ đi 1, nếu gửi page=0 hoặc âm thì để nguyên 0
        int currentPage = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(currentPage, size);
        
        Page<LichSuHoatDongDTO> result = giaoCaService.getLichSu(employee, tenCa, startDate, endDate, pageable);
        
        return ResponseEntity.ok(result);
    }
}