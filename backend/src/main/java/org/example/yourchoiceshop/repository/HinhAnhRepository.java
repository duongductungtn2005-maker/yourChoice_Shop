package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import
import org.springframework.data.jpa.repository.Query;     // Import
import org.springframework.transaction.annotation.Transactional;

public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer> {

    // --- THÊM HÀM NÀY ---
    @Modifying
    @Transactional
    @Query("DELETE FROM HinhAnh h WHERE h.chiTietSanPham.id = :variantId")
    void deleteByChiTietSanPhamId(Integer variantId);
}