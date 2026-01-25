package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    // SỬA LẠI ĐOẠN QUERY NÀY
    @Query("SELECT n FROM NhanVien n WHERE " +
       "(:keyword IS NULL OR " +
       " LOWER(n.tenNhanVien) LIKE :keyword OR " +
       " LOWER(n.email) LIKE :keyword OR " +
       " n.soDienThoai LIKE :keyword) " + // SĐT không cần Lower vì là số
       "AND (:gender IS NULL OR n.gioiTinh = :gender) " +
       "AND (:status IS NULL OR n.trangThai = :status)")
    Page<NhanVien> searchNhanVien(
        @Param("keyword") String keyword, 
        @Param("gender") Boolean gender, 
        @Param("status") Integer status, 
        Pageable pageable
    );
}