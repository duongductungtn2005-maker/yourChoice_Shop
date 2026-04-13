package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {

    List<ChatSession> findByLoaiChatAndTrangThaiOrderByNgayCapNhatDesc(String loaiChat, Integer trangThai);

    List<ChatSession> findByLoaiChatOrderByNgayCapNhatDesc(String loaiChat);

    Optional<ChatSession> findBySessionIdAndTrangThaiNot(String sessionId, Integer trangThai);

    Optional<ChatSession> findByKhachHangIdAndTrangThaiNot(Integer khachHangId, Integer trangThai);

    @Query("SELECT s FROM ChatSession s WHERE s.loaiChat = :loaiChat ORDER BY s.ngayCapNhat DESC")
    List<ChatSession> findByLoaiChat(@Param("loaiChat") String loaiChat);
}
