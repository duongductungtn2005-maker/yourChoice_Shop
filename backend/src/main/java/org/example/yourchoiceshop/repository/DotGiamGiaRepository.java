package org.example.yourchoiceshop.repository;


import org.example.yourchoiceshop.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Modifying
    @Transactional
    @Query("UPDATE DotGiamGia d SET d.trangThai = 1 WHERE d.ngayBatDau <= :now AND d.ngayKetThuc > :now AND d.trangThai != 1")
    void updateStatusToActive(LocalDateTime now);

    // 2. Tắt các đợt đã hết giờ (Đang diễn ra -> Kết thúc)
    @Modifying
    @Transactional
    @Query("UPDATE DotGiamGia d SET d.trangThai = 0 WHERE d.ngayKetThuc <= :now AND d.trangThai = 1")
    void updateStatusToExpired(LocalDateTime now);

    @Query("SELECT d FROM DotGiamGia d JOIN ChiTietDotGiamGia ct ON d.id = ct.dotGiamGia.id " +
            "WHERE ct.chiTietSanPham.id = :productId " +
            "AND d.trangThai = 1 " +
            "AND :now BETWEEN d.ngayBatDau AND d.ngayKetThuc")
    List<DotGiamGia> findValidPromotionsForProduct(@Param("productId") Integer productId, @Param("now") LocalDateTime now);
}