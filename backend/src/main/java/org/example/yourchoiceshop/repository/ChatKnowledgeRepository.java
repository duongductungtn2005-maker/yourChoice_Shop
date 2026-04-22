package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChatKnowledge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatKnowledgeRepository extends JpaRepository<ChatKnowledge, Integer> {

    List<ChatKnowledge> findByTrangThaiTrueOrderByDoUuTienDescIdDesc();

    List<ChatKnowledge> findAllByOrderByDoUuTienDescIdDesc();

    @Query("SELECT k FROM ChatKnowledge k " +
            "WHERE k.trangThai = true AND (" +
            "LOWER(k.cauHoiMau) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(COALESCE(k.tuKhoa, '')) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") ORDER BY k.doUuTien DESC, k.id DESC")
    List<ChatKnowledge> searchCandidates(@Param("query") String query, Pageable pageable);
}
