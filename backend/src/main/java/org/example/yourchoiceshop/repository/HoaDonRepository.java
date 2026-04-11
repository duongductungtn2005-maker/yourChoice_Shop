package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.dto.response.CustomerStatDTO;
import org.example.yourchoiceshop.dto.response.VoucherStatDTO;
import org.example.yourchoiceshop.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    @EntityGraph(attributePaths = {"khachHang", "nhanVien"})
    @Query("""
            SELECT h FROM HoaDon h
            WHERE
                (:keyword IS NULL OR h.maHoaDon LIKE %:keyword% OR h.tenNguoiNhan LIKE %:keyword%)
                AND h.trangThai <> 9
                AND (:status IS NULL OR h.trangThai = :status)
                AND (:type IS NULL OR h.loaiHoaDon = :type)
                AND (:khachHangId IS NULL OR h.khachHang.id = :khachHangId)
                AND (:fromDate IS NULL OR h.ngayTao >= :fromDate)
                AND (:toDate IS NULL OR h.ngayTao <= :toDate)
            """)
    Page<HoaDon> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("type") String type,
            @Param("khachHangId") Integer khachHangId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"hoaDonChiTiets", "hoaDonChiTiets.chiTietSanPham"})
    @Query("""
            SELECT DISTINCT h FROM HoaDon h
            WHERE h.trangThai = 1
            AND EXISTS (
                SELECT 1 FROM HoaDonChiTiet ct
                WHERE ct.hoaDon = h AND ct.chiTietSanPham.id IN :productIds
            )
            """)
    List<HoaDon> findPendingOrdersByProductIds(@Param("productIds") Set<Integer> productIds);

    // Tra cứu đơn hàng theo SĐT + mã đơn (cho khách vãng lai)
    @EntityGraph(attributePaths = {"khachHang", "nhanVien"})
    @Query("""
            SELECT h FROM HoaDon h
            WHERE h.sdtNguoiNhan = :soDienThoai
                AND h.trangThai <> 9
                AND (:keyword IS NULL OR h.maHoaDon LIKE %:keyword%)
                AND (:status IS NULL OR h.trangThai = :status)
            ORDER BY h.ngayTao DESC
            """)
    Page<HoaDon> searchOrdersByPhone(
            @Param("soDienThoai") String soDienThoai,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );

    // =========================
    // THỐNG KÊ VOUCHER HOT
    // =========================
    @Query(value = """
            SELECT 
                p.ten_phieu_giam_gia AS tenPhieu,
                COUNT(h.id) AS soLuotDung
            FROM hoa_don h
            JOIN phieu_giam_gia p ON h.id_phieu_giam_gia = p.id
            WHERE h.ngay_tao >= :fromDate
              AND h.ngay_tao <= :toDate
              AND h.trang_thai != 0
            GROUP BY p.id, p.ten_phieu_giam_gia
            ORDER BY COUNT(h.id) DESC
            """, nativeQuery = true)
    List<VoucherStatDTO> getVoucherHot(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // =========================
    // TOP KHÁCH HÀNG
    // =========================
    @Query("""
            SELECT new org.example.yourchoiceshop.dto.response.CustomerStatDTO(
                k.maKhachHang,
                k.tenKhachHang,
                COUNT(h.id),
                COALESCE(SUM(h.tongTienSauGiam), 0)
            )
            FROM HoaDon h
            JOIN h.khachHang k
            WHERE h.trangThai = 5
            GROUP BY k.maKhachHang, k.tenKhachHang
            ORDER BY COALESCE(SUM(h.tongTienSauGiam), 0) DESC
            """)
    List<CustomerStatDTO> getTopCustomers(Pageable pageable);
}