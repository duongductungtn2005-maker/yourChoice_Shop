package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatSessionIdOrderByNgayGuiAsc(Integer chatSessionId);
}
