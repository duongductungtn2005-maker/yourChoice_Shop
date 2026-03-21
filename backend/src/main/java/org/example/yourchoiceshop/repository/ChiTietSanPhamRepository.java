package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    // Tìm biến thể theo mã CTSP (dùng cho quét QR) - chỉ lấy sản phẩm đang kinh doanh
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.maCtsp = :maCtsp AND c.trangThai = 1 AND c.sanPham.trangThai = 1")
    Optional<ChiTietSanPham> findByMaCtspAndActive(@Param("maCtsp") String maCtsp);

    // Lấy tất cả con theo cha
    List<ChiTietSanPham> findBySanPhamId(Integer sanPhamId);

    // Tính tổng tồn kho
    @Query("SELECT SUM(c.soLuong) FROM ChiTietSanPham c WHERE c.sanPham.id = :id")
    Integer sumSoLuongBySanPhamId(@Param("id") Integer id);

    // --- HÀM LỌC BIẾN THỂ NÂNG CAO ---
    @Query("SELECT c FROM ChiTietSanPham c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR c.sanPham.tenSanPham LIKE %:keyword% OR c.maCtsp LIKE %:keyword%) " +
            "AND (:idMauSac IS NULL OR c.mauSac.id = :idMauSac) " +
            "AND (:idKichThuoc IS NULL OR c.kichThuoc.id = :idKichThuoc) " +
            "AND (:idThuongHieu IS NULL OR c.thuongHieu.id = :idThuongHieu) " +
            "AND (:idChatLieu IS NULL OR c.chatLieu.id = :idChatLieu) " +
            "AND (:idCoAo IS NULL OR c.coAo.id = :idCoAo) " +
            "AND (:idTayAo IS NULL OR c.tayAo.id = :idTayAo) " +
            "AND (:idXuatXu IS NULL OR c.xuatXu.id = :idXuatXu) " +
            "AND (:idSanPham IS NULL OR c.sanPham.id = :idSanPham) " +
            "AND (:trangThai IS NULL OR c.trangThai = :trangThai)")
    Page<ChiTietSanPham> searchByCriteria(
            @Param("keyword") String keyword,
            @Param("idMauSac") Integer idMauSac,
            @Param("idKichThuoc") Integer idKichThuoc,
            @Param("idThuongHieu") Integer idThuongHieu,
            @Param("idChatLieu") Integer idChatLieu,
            @Param("idCoAo") Integer idCoAo,
            @Param("idTayAo") Integer idTayAo,
            @Param("idXuatXu") Integer idXuatXu,
            @Param("idSanPham") Integer idSanPham,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE ChiTietSanPham c SET c.soLuong = c.soLuong - :quantity WHERE c.id = :id AND c.soLuong >= :quantity")
        int reserveStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE ChiTietSanPham c SET c.soLuong = c.soLuong + :quantity WHERE c.id = :id")
        int releaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    // --- AI CHAT: Tìm kiếm nâng cao toàn diện ---
    @Query("SELECT DISTINCT c FROM ChiTietSanPham c " +
            "LEFT JOIN c.sanPham sp " +
            "LEFT JOIN c.mauSac ms " +
            "LEFT JOIN c.kichThuoc kt " +
            "LEFT JOIN c.dotGiamGia dgg " +
            "WHERE c.trangThai = 1 AND sp.trangThai = 1 " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "     OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "     OR LOWER(sp.moTaChiTiet) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "     OR LOWER(c.maCtsp) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
            "AND (:colorName IS NULL OR :colorName = '' " +
            "     OR LOWER(ms.tenMauSac) LIKE LOWER(CONCAT('%',:colorName,'%'))) " +
            "AND (:sizeName IS NULL OR :sizeName = '' " +
            "     OR LOWER(kt.tenKichThuoc) LIKE LOWER(CONCAT('%',:sizeName,'%'))) " +
            "AND (:minPrice IS NULL OR c.giaBan >= :minPrice) " +
            "AND (:maxPrice IS NULL OR c.giaBan <= :maxPrice) " +
            "AND (:onlyDiscount = false OR (dgg IS NOT NULL AND dgg.trangThai = 1 " +
            "     AND dgg.ngayBatDau <= CURRENT_TIMESTAMP AND dgg.ngayKetThuc >= CURRENT_TIMESTAMP)) " +
            "AND c.soLuong > 0")
    List<ChiTietSanPham> aiSmartSearch(
            @Param("keyword") String keyword,
            @Param("colorName") String colorName,
            @Param("sizeName") String sizeName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("onlyDiscount") boolean onlyDiscount,
            Pageable pageable
    );

    // --- AI CHAT: Tìm sản phẩm đang giảm giá ---
    @Query("SELECT c FROM ChiTietSanPham c " +
            "JOIN c.dotGiamGia dgg " +
            "WHERE c.trangThai = 1 AND c.sanPham.trangThai = 1 " +
            "AND dgg.trangThai = 1 " +
            "AND dgg.ngayBatDau <= CURRENT_TIMESTAMP " +
            "AND dgg.ngayKetThuc >= CURRENT_TIMESTAMP " +
            "AND c.soLuong > 0 " +
            "ORDER BY dgg.giaTriGiam DESC")
    List<ChiTietSanPham> findOnSaleProducts(Pageable pageable);

    // --- AI CHAT: Tìm sản phẩm tương tự (cùng loại / thương hiệu) ---
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.trangThai = 1 AND c.sanPham.trangThai = 1 " +
            "AND c.sanPham.id != :excludeProductId " +
            "AND (c.sanPham.thuongHieu.id = :brandId OR c.sanPham.chatLieu.id = :materialId) " +
            "AND c.soLuong > 0")
    List<ChiTietSanPham> findSimilarProducts(
            @Param("excludeProductId") Integer excludeProductId,
            @Param("brandId") Integer brandId,
            @Param("materialId") Integer materialId,
            Pageable pageable
    );
}