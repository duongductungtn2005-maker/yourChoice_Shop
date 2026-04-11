package org.example.yourchoiceshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.ChatMessageRequest;
import org.example.yourchoiceshop.dto.ChatMessageResponse;
import org.example.yourchoiceshop.dto.ChatSessionResponse;
import org.example.yourchoiceshop.entity.ChatSession;
import org.example.yourchoiceshop.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Lấy hoặc tạo session chat
     */
    @PostMapping("/session")
    public ResponseEntity<ChatSessionResponse> getOrCreateSession(
            @RequestParam(required = false) Integer khachHangId,
            @RequestParam(required = false) String sessionKey,
            @RequestParam(required = false) String tenHienThi
    ) {
        ChatSession session = chatService.getOrCreateSession(khachHangId, sessionKey, tenHienThi);
        return ResponseEntity.ok(chatService.toSessionResponse(session));
    }

    /**
     * Lấy thông tin session theo ID (polling)
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getSessionInfo(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(chatService.getSessionInfo(sessionId));
    }

    /**
     * Lấy danh sách session theo loại ("KHACH_HANG" hoặc "NOI_BO") và trạng thái
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponse>> getSessions(
            @RequestParam(defaultValue = "KHACH_HANG") String loaiChat,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(chatService.getSessions(loaiChat, trangThai));
    }

    /**
     * Lấy lịch sử tin nhắn
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }

    /**
     * Gửi tin nhắn (khách hàng hoặc nhân viên)
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    /**
     * Nhân viên nhận xử lý session
     */
    @PostMapping("/sessions/{sessionId}/assign")
    public ResponseEntity<ChatSessionResponse> assignStaff(
            @PathVariable Integer sessionId,
            @RequestParam Integer nhanVienId
    ) {
        ChatSession session = chatService.assignStaff(sessionId, nhanVienId);
        return ResponseEntity.ok(chatService.toSessionResponse(session));
    }

    /**
     * Đóng session
     */
    @PostMapping("/sessions/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable Integer sessionId) {
        chatService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Tạo session chat nội bộ giữa nhân viên
     */
    @PostMapping("/internal/session")
    public ResponseEntity<ChatSessionResponse> createInternalSession(
            @RequestParam Integer nhanVienId,
            @RequestParam(required = false) String tieuDe
    ) {
        ChatSession session = chatService.createInternalSession(nhanVienId, tieuDe);
        return ResponseEntity.ok(chatService.toSessionResponse(session));
    }

    /**
     * Lấy danh sách nhân viên (dùng cho chat nội bộ)
     */
    @GetMapping("/staff-list")
    public ResponseEntity<List<Map<String, Object>>> getStaffList() {
        return ResponseEntity.ok(chatService.getStaffList());
    }
}
