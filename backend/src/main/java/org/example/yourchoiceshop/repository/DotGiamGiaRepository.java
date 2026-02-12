package org.example.yourchoiceshop.repository;


import org.example.yourchoiceshop.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {

    // --- ĐÃ XÓA "ORDER BY d.id DESC" ---
    // Lý do: Pageable truyền vào đã có sẵn thông tin Sort rồi.
    @Query("SELECT d FROM DotGiamGia d WHERE " +
            "(:keyword IS NULL OR d.maDotGiamGia LIKE %:keyword% OR d.tenDotGiamGia LIKE %:keyword%) " +
            "AND (:status IS NULL OR d.trangThai = :status) " +
            "AND (cast(:startDate as timestamp) IS NULL OR d.ngayBatDau >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR d.ngayKetThuc <= :endDate)")
    Page<DotGiamGia> search(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    boolean existsByMaDotGiamGia(String ma);
}