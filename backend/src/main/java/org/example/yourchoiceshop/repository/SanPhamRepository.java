package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> upstream/main

// 1. Repository Sản Phẩm (Cha)
@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    // Tìm kiếm theo tên hoặc mã, hỗ trợ phân trang
    @Query("SELECT sp FROM SanPham sp WHERE " +
            "(:keyword IS NULL OR sp.tenSanPham LIKE %:keyword% OR sp.maSanPham LIKE %:keyword%) " +
            "AND sp.trangThai = 1")
    Page<SanPham> searchSanPhams(String keyword, Pageable pageable);

    boolean existsByMaSanPham(String maSanPham);
}