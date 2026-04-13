package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.service.ThongBaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/thong-bao")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        return ResponseEntity.ok(Map.of(
                "items", thongBaoService.getRecent(),
                "unreadCount", thongBaoService.countUnread()
        ));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id) {
        thongBaoService.markAsRead(id);
        return ResponseEntity.ok("OK");
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        thongBaoService.markAllAsRead();
        return ResponseEntity.ok("OK");
    }
}
