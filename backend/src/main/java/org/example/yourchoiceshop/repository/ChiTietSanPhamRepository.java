package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    // Lấy tất cả con theo cha
    List<ChiTietSanPham> findBySanPhamId(Integer sanPhamId);

    // Tính tổng tồn kho (Optional - để hiển thị thống kê)
    @Query("SELECT SUM(c.soLuong) FROM ChiTietSanPham c WHERE c.sanPham.id = :id")
    Integer sumSoLuongBySanPhamId(@Param("id") Integer id);
}