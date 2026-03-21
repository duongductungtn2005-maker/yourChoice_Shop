package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.GiaoCa;
import org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface GiaoCaRepository extends JpaRepository<GiaoCa, Integer> {

    @Query(value = "SELECT new org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO(" +
           "llv.id, nv.tenNhanVien, nv.maNhanVien, ca.tenCa, " +
           "gc.thoiGianNhanCa, gc.thoiGianGiaoCa, COALESCE(gc.trangThai, 0), gc.ghiChu) " +
           "FROM LichLamViec llv " +
           "JOIN llv.nhanVien nv " +
           "JOIN llv.caLamViec ca " +
           "LEFT JOIN GiaoCa gc ON gc.lichLamViec.id = llv.id " +
           "WHERE (:search IS NULL OR :search = '' OR LOWER(nv.tenNhanVien) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:tenCa IS NULL OR :tenCa = '' OR LOWER(ca.tenCa) LIKE LOWER(CONCAT('%', :tenCa, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR llv.ngayLamViec >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR llv.ngayLamViec <= :endDate) " +
           "ORDER BY llv.ngayLamViec DESC, ca.thoiGianBatDau ASC",
           
           countQuery = "SELECT COUNT(llv.id) " +
           "FROM LichLamViec llv " +
           "JOIN llv.nhanVien nv " +
           "JOIN llv.caLamViec ca " +
           "WHERE (:search IS NULL OR :search = '' OR LOWER(nv.tenNhanVien) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:tenCa IS NULL OR :tenCa = '' OR LOWER(ca.tenCa) LIKE LOWER(CONCAT('%', :tenCa, '%'))) " +
           "AND (CAST(:startDate AS date) IS NULL OR llv.ngayLamViec >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR llv.ngayLamViec <= :endDate)")
    Page<LichSuHoatDongDTO> findLichSuHoatDong(
            @Param("search") String search,
            @Param("tenCa")  String tenCa,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
            // Tìm ca đang làm việc (trạng thái = 1) của nhân viên hiện tại
    Optional<GiaoCa> findByNhanVienTrongCa_IdAndTrangThai(Integer idNhanVien, Integer trangThai);
    // Tìm ca đang mở của một lịch cụ thể
Optional<GiaoCa> findByLichLamViec_IdAndTrangThai(Integer idLich, Integer trangThai);
}