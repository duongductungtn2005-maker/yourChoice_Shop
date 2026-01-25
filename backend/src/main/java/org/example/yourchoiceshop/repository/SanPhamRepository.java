package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    // Query tìm kiếm nâng cao (Full option)
    @Query("SELECT s FROM SanPham s WHERE " +
            "(:keyword IS NULL OR s.tenSanPham LIKE %:keyword% OR s.maSanPham LIKE %:keyword%) " +
            "AND (:status IS NULL OR s.trangThai = :status) " +
            "AND (:idThuongHieu IS NULL OR s.thuongHieu.id = :idThuongHieu) " +
            "AND (:idChatLieu IS NULL OR s.chatLieu.id = :idChatLieu) " +
            "AND (:idXuatXu IS NULL OR s.xuatXu.id = :idXuatXu) " +
            "AND (:idCoAo IS NULL OR s.coAo.id = :idCoAo) " +
            "AND (:idTayAo IS NULL OR s.tayAo.id = :idTayAo)")
    Page<SanPham> searchProductsAdvanced(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("idThuongHieu") Integer idThuongHieu,
            @Param("idChatLieu") Integer idChatLieu,
            @Param("idXuatXu") Integer idXuatXu,
            @Param("idCoAo") Integer idCoAo,
            @Param("idTayAo") Integer idTayAo,
            Pageable pageable
    );
}