package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {

    // 1. Hàm dùng để hiển thị lên Modal (Chỉ lấy cái đang hoạt động)
    @Query("SELECT ct FROM ChiTietDotGiamGia ct " +
            "WHERE ct.dotGiamGia.id = :saleId AND ct.trangThai = 1")
    List<ChiTietDotGiamGia> findActiveProductsBySaleId(@Param("saleId") Integer saleId);

    // 2. HÀM CẦN THÊM ĐỂ FIX LỖI (Dùng cho Update/Delete trong Service)
    // Spring Data JPA sẽ tự động hiểu câu lệnh này mà không cần @Query
    List<ChiTietDotGiamGia> findByDotGiamGiaId(Integer idDotGiamGia);
}