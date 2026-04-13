package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {

    // Hàm này đã cover cả việc tìm kiếm VÀ lọc trạng thái
    @Query("SELECT x FROM XuatXu x WHERE " +
            "(:keyword IS NULL OR x.tenXuatXu LIKE %:keyword%) " +
            "AND (:status IS NULL OR x.trangThai = :status)")
    Page<XuatXu> search(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );
}