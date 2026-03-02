package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    // THÊM DÒNG NÀY ĐỂ TỐI ƯU QUERY KHÁCH HÀNG
    @EntityGraph(attributePaths = {"khachHang"})
    @Query("SELECT h FROM HoaDon h WHERE " +
            "(:keyword IS NULL OR h.maHoaDon LIKE %:keyword% OR h.tenNguoiNhan LIKE %:keyword%) AND " +
            "(:status IS NULL OR h.trangThai = :status) AND " +
            "(:type IS NULL OR h.loaiHoaDon = :type) AND " +
            "(:fromDate IS NULL OR h.ngayTao >= :fromDate) AND " +
            "(:toDate IS NULL OR h.ngayTao <= :toDate)")
    Page<HoaDon> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("type") String type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}