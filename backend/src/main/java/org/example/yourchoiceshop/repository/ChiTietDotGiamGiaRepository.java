package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietDotGiamGia;
import org.example.yourchoiceshop.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {
    // Tìm các sản phẩm thuộc đợt giảm giá này để xóa/sửa
    List<ChiTietDotGiamGia> findByDotGiamGiaId(Integer idDot);

    @Query("SELECT d FROM ChiTietDotGiamGia c " +
       "JOIN c.dotGiamGia d " +
       "WHERE c.chiTietSanPham.id = :idCtsp " +
       "AND d.trangThai = 1 " +
       "AND CURRENT_TIMESTAMP BETWEEN d.ngayBatDau AND d.ngayKetThuc " +
         "ORDER BY CASE WHEN d.loaiGiamGia = '%' THEN (c.chiTietSanPham.giaBan * d.giaTriGiam / 100) ELSE d.giaTriGiam END DESC, d.giaTriGiam DESC, d.id DESC")
List<DotGiamGia> findBestActiveDiscountForProduct(@Param("idCtsp") Integer idCtsp);
}