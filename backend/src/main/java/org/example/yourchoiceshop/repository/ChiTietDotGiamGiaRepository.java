package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {
    // Tìm các sản phẩm thuộc đợt giảm giá này để xóa/sửa
    List<ChiTietDotGiamGia> findByDotGiamGiaId(Integer idDot);
}