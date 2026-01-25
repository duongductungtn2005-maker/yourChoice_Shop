package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    // Lấy danh sách biến thể của 1 sản phẩm
    @Query("SELECT ct FROM ChiTietSanPham ct WHERE ct.sanPham.id = :idSanPham AND ct.trangThai = 1")
    Page<ChiTietSanPham> findBySanPhamId(Integer idSanPham, Pageable pageable);
}