package org.example.yourchoiceshop.repository;

import org.springframework.data.domain.Pageable;
import org.example.yourchoiceshop.entity.GiaoCa;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GiaoCaRepository extends JpaRepository<GiaoCa, Integer> {
    
    @Query(value = "SELECT gc.id, nv.ten_nhan_vien, nv.ma_nhan_vien, clv.ten_ca, " +
           "gc.thoi_gian_nhan_ca, gc.thoi_gian_giao_ca, gc.trang_thai, gc.ghi_chu " +
           "FROM giao_ca gc " +
           "JOIN nhan_vien nv ON gc.id_nhan_vien_trong_ca = nv.id " +
           "LEFT JOIN lich_lam_viec llv ON gc.id_lich_lam_viec = llv.id " +
           "LEFT JOIN ca_lam_viec clv ON llv.id_ca_lam_viec = clv.id " +
           "WHERE (:search IS NULL OR nv.ten_nhan_vien LIKE %:search% OR nv.ma_nhan_vien LIKE %:search%) " +
           "AND (:tenCa IS NULL OR clv.ten_ca LIKE %:tenCa%) " +
           "ORDER BY gc.thoi_gian_nhan_ca DESC", nativeQuery = true)
    Page<Object[]> findLichSuHoatDong(@Param("search") String search, 
                                     @Param("tenCa") String tenCa, 
                                     Pageable pageable);
}