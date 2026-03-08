package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    // Lấy tất cả con theo cha
    List<ChiTietSanPham> findBySanPhamId(Integer sanPhamId);

    // Tính tổng tồn kho
    @Query("SELECT SUM(c.soLuong) FROM ChiTietSanPham c WHERE c.sanPham.id = :id")
    Integer sumSoLuongBySanPhamId(@Param("id") Integer id);

    // --- [THÊM MỚI] HÀM LỌC SẢN PHẨM ---
    @Query("SELECT c FROM ChiTietSanPham c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR c.sanPham.tenSanPham LIKE %:keyword% OR c.maCtsp LIKE %:keyword%) " +
            "AND (:idMauSac IS NULL OR c.mauSac.id = :idMauSac) " +
            "AND (:idKichThuoc IS NULL OR c.kichThuoc.id = :idKichThuoc) " +
            "AND (:trangThai IS NULL OR c.trangThai = :trangThai)")
    Page<ChiTietSanPham> searchByCriteria(
            @Param("keyword") String keyword,
            @Param("idMauSac") Integer idMauSac,
            @Param("idKichThuoc") Integer idKichThuoc,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE ChiTietSanPham c SET c.soLuong = c.soLuong - :quantity WHERE c.id = :id AND c.soLuong >= :quantity")
        int reserveStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE ChiTietSanPham c SET c.soLuong = c.soLuong + :quantity WHERE c.id = :id")
        int releaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
}