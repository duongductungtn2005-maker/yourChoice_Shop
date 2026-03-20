package org.example.yourchoiceshop.controller;

import org.example.yourchoiceshop.dto.request.ManualReportRequest;
import org.example.yourchoiceshop.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/send-manual")
    public ResponseEntity<?> sendManualReport(@RequestBody ManualReportRequest request) {
        if (request.getEmails() == null || request.getEmails().isEmpty()) {
            return ResponseEntity.badRequest().body("Danh sách email trống!");
        }
        try {
            reportService.generateAndSendReport(request.getEmails(), request.getStartTime(), request.getEndTime());
            return ResponseEntity.ok("Gửi báo cáo thành công!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}