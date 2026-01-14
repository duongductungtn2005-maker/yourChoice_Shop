package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ChatLieu;
import org.example.yourchoiceshop.repository.ChatLieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatLieuServiceImpl {

    private final ChatLieuRepository chatLieuRepository;

    public Page<ChatLieu> getAll(Pageable pageable) {
        return chatLieuRepository.findAll(pageable);
    }

    public ChatLieu getById(Integer id) {
        return chatLieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu ID: " + id));
    }

    public ChatLieu create(StoreAttributeRequest request) {
        ChatLieu chatLieu = new ChatLieu();
        // Tự sinh mã: CL + thời gian (Ví dụ: CL17099988)
        chatLieu.setMaChatLieu(request.getMa() != null && !request.getMa().isEmpty()
                ? request.getMa()
                : "CL" + System.currentTimeMillis());

        chatLieu.setTenChatLieu(request.getTen());
        chatLieu.setTrangThai(request.getTrangThai());

        return chatLieuRepository.save(chatLieu);
    }

    public ChatLieu update(Integer id, StoreAttributeRequest request) {
        ChatLieu chatLieu = getById(id);
        chatLieu.setTenChatLieu(request.getTen());
        chatLieu.setTrangThai(request.getTrangThai());
        return chatLieuRepository.save(chatLieu);
    }

    public void delete(Integer id) {
        ChatLieu chatLieu = getById(id);
        chatLieu.setTrangThai(0);
        chatLieuRepository.save(chatLieu);
    }
}