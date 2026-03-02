package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; // Nhớ import List

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    @Query("SELECT n FROM NhanVien n WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(n.tenNhanVien) LIKE %:keyword% OR " +
            " LOWER(n.email) LIKE %:keyword% OR " +
            " n.soDienThoai LIKE %:keyword%) " +
            "AND (:status IS NULL OR n.trangThai = :status) " +
            // SỬA LẠI ĐOẠN NÀY: Dùng điều kiện OR thay vì COALESCE
            "AND (:roleIds IS NULL OR n.quyenHan.id IN :roleIds)")
    Page<NhanVien> searchNhanVien(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("roleIds") List<Integer> roleIds,
            Pageable pageable
    );
        boolean existsByTenTaiKhoan(String tenTaiKhoan);
        boolean existsByTenTaiKhoanAndIdNot(String tenTaiKhoan, Integer id);
}