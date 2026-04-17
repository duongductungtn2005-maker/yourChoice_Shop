package org.example.yourchoiceshop.service;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.ChatMessageRequest;
import org.example.yourchoiceshop.dto.ChatMessageResponse;
import org.example.yourchoiceshop.dto.ChatSessionResponse;
import org.example.yourchoiceshop.entity.ChatMessage;
import org.example.yourchoiceshop.entity.ChatSession;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.repository.ChatMessageRepository;
import org.example.yourchoiceshop.repository.ChatSessionRepository;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ChatAiService chatAiService;
    private final SimpMessagingTemplate messagingTemplate;

    /* ======================== SESSION ======================== */

    /**
     * Lấy hoặc tạo session cho khách hàng / khách vãng lai
     */
    @Transactional
    public ChatSession getOrCreateSession(Integer khachHangId, String sessionKey, String tenHienThi) {
        // Khách hàng đã đăng nhập
        if (khachHangId != null) {
            Optional<ChatSession> existing = chatSessionRepository.findByKhachHangIdAndTrangThaiNot(khachHangId, 3);
            if (existing.isPresent()) return existing.get();

            KhachHang kh = khachHangRepository.findById(khachHangId).orElse(null);
            ChatSession session = new ChatSession();
            session.setLoaiChat("KHACH_HANG");
            session.setTrangThai(1);
            session.setTenHienThi(kh != null ? kh.getTenKhachHang() : (tenHienThi != null ? tenHienThi : "Khách hàng"));
            session.setKhachHang(kh);
            session.setNguoiXuLy("AI");
            return chatSessionRepository.save(session);
        }

        // Khách vãng lai (anonymous)
        if (sessionKey != null) {
            Optional<ChatSession> existing = chatSessionRepository.findBySessionIdAndTrangThaiNot(sessionKey, 3);
            if (existing.isPresent()) return existing.get();
        }

        ChatSession session = new ChatSession();
        session.setLoaiChat("KHACH_HANG");
        session.setTrangThai(1);
        session.setTenHienThi(tenHienThi != null ? tenHienThi : "Khách vãng lai");
        session.setSessionId(sessionKey != null ? sessionKey : UUID.randomUUID().toString());
        session.setNguoiXuLy("AI");
        return chatSessionRepository.save(session);
    }

    /**
     * Lấy danh sách session theo loại và trạng thái
     */
    public List<ChatSessionResponse> getSessions(String loaiChat, Integer trangThai) {
        List<ChatSession> sessions;
        if (trangThai != null) {
            sessions = chatSessionRepository.findByLoaiChatAndTrangThaiOrderByNgayCapNhatDesc(loaiChat, trangThai);
        } else {
            sessions = chatSessionRepository.findByLoaiChatOrderByNgayCapNhatDesc(loaiChat);
        }
        return sessions.stream().map(this::toSessionResponse).collect(Collectors.toList());
    }

    /**
     * Đóng session
     */
    @Transactional
    public void closeSession(Integer sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow();
        session.setTrangThai(3); // Đã đóng
        chatSessionRepository.save(session);
    }

    /**
     * Lấy thông tin session theo ID
     */
    public ChatSessionResponse getSessionInfo(Integer sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow();
        return toSessionResponse(session);
    }

    /**
     * Nhân viên nhận xử lý session (chuyển từ AI sang nhân viên)
     */
    @Transactional
    public ChatSession assignStaff(Integer sessionId, Integer nhanVienId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow();
        NhanVien nv = nhanVienRepository.findById(nhanVienId).orElse(null);
        session.setNhanVien(nv);
        session.setNguoiXuLy(nv != null ? nv.getTenNhanVien() : "Nhân viên");
        session.setTrangThai(1); // Đang hoạt động
        chatSessionRepository.save(session);

        // Gửi tin nhắn hệ thống
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setChatSession(session);
        systemMsg.setSenderRole("SYSTEM");
        systemMsg.setSenderName("Hệ thống");
        systemMsg.setNoiDung("Nhân viên " + (nv != null ? nv.getTenNhanVien() : "") + " đã tham gia cuộc trò chuyện.");
        systemMsg.setLoaiTinNhan("TEXT");
        chatMessageRepository.save(systemMsg);

        return session;
    }

    /* ======================== MESSAGES ======================== */

    /**
     * Lấy lịch sử tin nhắn của session
     */
    public List<ChatMessageResponse> getMessages(Integer sessionId) {
        return chatMessageRepository.findByChatSessionIdOrderByNgayGuiAsc(sessionId)
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    /* ======================== CHAT NỘI BỘ ======================== */

    /**
     * Tạo session chat nội bộ
     */
    @Transactional
    public ChatSession createInternalSession(Integer nhanVienId, String tieuDe) {
        NhanVien nv = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        ChatSession session = new ChatSession();
        session.setLoaiChat("NOI_BO");
        session.setTrangThai(1);
        session.setTenHienThi(tieuDe != null && !tieuDe.isBlank() ? tieuDe : nv.getTenNhanVien());
        session.setNhanVien(nv);
        session.setNguoiXuLy(nv.getTenNhanVien());
        chatSessionRepository.save(session);

        // Tin nhắn hệ thống khi tạo cuộc trò chuyện
        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setChatSession(session);
        systemMsg.setSenderRole("SYSTEM");
        systemMsg.setSenderName("Hệ thống");
        systemMsg.setNoiDung(nv.getTenNhanVien() + " đã tạo cuộc trò chuyện nội bộ.");
        systemMsg.setLoaiTinNhan("TEXT");
        chatMessageRepository.save(systemMsg);

        return session;
    }

    /**
     * Lấy danh sách nhân viên (cho chức năng tạo chat nội bộ)
     */
    public List<Map<String, Object>> getStaffList() {
        return nhanVienRepository.findAll()
                .stream()
                .filter(nv -> nv.getTrangThai() != null && nv.getTrangThai() == 1) // Chỉ nhân viên đang hoạt động
                .map(nv -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", nv.getId());
                    m.put("tenNhanVien", nv.getTenNhanVien());
                    m.put("maNhanVien", nv.getMaNhanVien());
                    m.put("chucVu", nv.getQuyenHan() != null ? nv.getQuyenHan().getTenQuyenHan() : null);
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gửi tin nhắn từ khách hàng / nhân viên
     */
    @Transactional
    public Map<String, Object> sendMessage(ChatMessageRequest request) {
        ChatSession session;

        if (request.getSessionId() != null) {
            session = chatSessionRepository.findById(request.getSessionId()).orElseThrow();
        } else {
            session = getOrCreateSession(request.getKhachHangId(), request.getSessionKey(), request.getSenderName());
        }

        // Lưu tin nhắn người gửi
        ChatMessage msg = new ChatMessage();
        msg.setChatSession(session);
        msg.setSenderRole(request.getSenderRole() != null ? request.getSenderRole() : "CUSTOMER");
        msg.setSenderName(request.getSenderName());
        msg.setNoiDung(request.getNoiDung());
        msg.setLoaiTinNhan("TEXT");

        if (request.getKhachHangId() != null) {
            msg.setKhachHang(khachHangRepository.findById(request.getKhachHangId()).orElse(null));
        }
        if (request.getNhanVienId() != null) {
            msg.setNhanVien(nhanVienRepository.findById(request.getNhanVienId()).orElse(null));
        }

        chatMessageRepository.save(msg);
        session.setNgayCapNhat(LocalDateTime.now());
        chatSessionRepository.save(session);

        ChatMessageResponse msgResponse = toMessageResponse(msg);

        // Broadcast tin nhắn qua WebSocket cho session này
        messagingTemplate.convertAndSend("/topic/chat/" + session.getId(), msgResponse);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("message", msgResponse);

        // Nếu AI đang xử lý và tin nhắn từ khách hàng => AI tự trả lời
        boolean isFromCustomer = "CUSTOMER".equals(msg.getSenderRole());
        boolean isAiHandling = "AI".equals(session.getNguoiXuLy());

        if (isFromCustomer && isAiHandling) {
            List<String> contextMessages = buildConversationContext(session.getId());
            Map<String, Object> aiResult = chatAiService.processMessage(request.getNoiDung(), contextMessages);

            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setChatSession(session);
            aiMsg.setSenderRole("AI");
            aiMsg.setSenderName("Trợ lý AI");
            aiMsg.setNoiDung((String) aiResult.get("reply"));

            List<?> products = (List<?>) aiResult.get("products");
            aiMsg.setLoaiTinNhan(products != null && !products.isEmpty() ? "PRODUCT_LIST" : "TEXT");
            chatMessageRepository.save(aiMsg);

            ChatMessageResponse aiResponse = toMessageResponse(aiMsg);
            messagingTemplate.convertAndSend("/topic/chat/" + session.getId(), aiResponse);

            result.put("aiReply", aiResponse);
            result.put("products", products);

            // Nếu AI gợi ý chuyển sang nhân viên
            if (Boolean.TRUE.equals(aiResult.get("transferToStaff"))) {
                session.setTrangThai(2); // Chờ nhận
                session.setNguoiXuLy("Chờ nhân viên");
                chatSessionRepository.save(session);
                result.put("transferToStaff", true);
            }
        }

        return result;
    }

    private List<String> buildConversationContext(Integer sessionId) {
        List<ChatMessage> recentMessages = chatMessageRepository
                .findTop8ByChatSessionIdOrderByNgayGuiDesc(sessionId);
        Collections.reverse(recentMessages);

        return recentMessages.stream()
                .filter(m -> m.getNoiDung() != null && !m.getNoiDung().isBlank())
                .map(m -> {
                    String role;
                    if ("CUSTOMER".equals(m.getSenderRole())) {
                        role = "Khach";
                    } else if ("AI".equals(m.getSenderRole())) {
                        role = "AI";
                    } else if ("STAFF".equals(m.getSenderRole())) {
                        role = "NhanVien";
                    } else {
                        role = "HeThong";
                    }
                    return role + ": " + m.getNoiDung();
                })
                .collect(Collectors.toList());
    }

    /* ======================== MAPPING ======================== */

    public ChatSessionResponse toSessionResponse(ChatSession s) {
        ChatSessionResponse r = new ChatSessionResponse();
        r.setId(s.getId());
        r.setLoaiChat(s.getLoaiChat());
        r.setTrangThai(s.getTrangThai());
        r.setTenHienThi(s.getTenHienThi());
        r.setNguoiXuLy(s.getNguoiXuLy());
        r.setNgayTao(s.getNgayTao());
        r.setNgayCapNhat(s.getNgayCapNhat());

        // Lấy tin nhắn cuối cùng
        List<ChatMessage> msgs = chatMessageRepository.findByChatSessionIdOrderByNgayGuiAsc(s.getId());
        if (!msgs.isEmpty()) {
            ChatMessage last = msgs.get(msgs.size() - 1);
            r.setLastMessage(last.getNoiDung());
        }
        return r;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.setId(m.getId());
        r.setSessionId(m.getChatSession() != null ? m.getChatSession().getId() : null);
        r.setNhanVienId(m.getNhanVien() != null ? m.getNhanVien().getId() : null);
        r.setSenderRole(m.getSenderRole());
        r.setSenderName(m.getSenderName());
        r.setNoiDung(m.getNoiDung());
        r.setLoaiTinNhan(m.getLoaiTinNhan());
        r.setNgayGui(m.getNgayGui());
        return r;
    }
}
