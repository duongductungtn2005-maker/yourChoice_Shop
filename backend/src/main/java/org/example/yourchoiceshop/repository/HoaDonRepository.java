package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse;
import org.example.yourchoiceshop.entity.HoaDon;
import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

@Query("""
    SELECT new org.example.yourchoiceshop.dto.response.QuanLyDonHangResponse(
        hd.id,
        hd.maHoaDon,
        kh.tenKhachHang,
        hd.tongTienSauGiam,
        hd.ngayTao,
        hd.loaiHoaDon,
        hd.trangThai
    )
    FROM HoaDon hd
    LEFT JOIN hd.khachHang kh
    WHERE (:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%
           OR kh.tenKhachHang LIKE %:keyword%)
    AND (:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon)
    AND (:trangThai IS NULL OR hd.trangThai = :trangThai)
    ORDER BY hd.ngayTao DESC
""")
    Page<QuanLyDonHangResponse> searchHoaDon(
        @Param("keyword") String keyword,
        @Param("loaiHoaDon") LoaiHoaDon loaiHoaDon,
        @Param("trangThai") Integer trangThai,
        Pageable pageable
    );
}