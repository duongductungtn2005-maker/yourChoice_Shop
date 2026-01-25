package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {
    // XÓA ĐOẠN "AND d.trangThai = 1"
    @Query("SELECT d FROM DotGiamGia d WHERE " +
            "(:keyword IS NULL OR d.tenDotGiamGia LIKE %:keyword%) " +
            "ORDER BY d.id DESC")
    Page<DotGiamGia> search(String keyword, Pageable pageable);
}