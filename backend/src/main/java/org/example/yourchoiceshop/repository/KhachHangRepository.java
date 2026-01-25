package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("SELECT n FROM KhachHang n WHERE " +
       "(:keyword IS NULL OR " +
       " LOWER(n.tenKhachHang) LIKE :keyword OR " +
       " LOWER(n.email) LIKE :keyword OR " +
       " n.soDienThoai LIKE :keyword) " + // SĐT không cần Lower vì là số
       "AND (:gender IS NULL OR n.gioiTinh = :gender) " +
       "AND (:status IS NULL OR n.trangThai = :status)")
    Page<KhachHang> searchKhachHang(
        @Param("keyword") String keyword, 
        @Param("gender") Boolean gender, 
        @Param("status") Integer status, 
        Pageable pageable
    );
}