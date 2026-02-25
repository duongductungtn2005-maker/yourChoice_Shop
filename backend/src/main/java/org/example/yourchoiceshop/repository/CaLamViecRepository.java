package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Integer> {

    @Query(value = "SELECT c FROM CaLamViec c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(c.tenCa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.maCa) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR c.trangThai = :status) AND " +
           // Dùng FUNCTION format để SQL Server đổi DATETIME ra chuỗi giờ phút 'HH:mm'
           "(:startTimeStr IS NULL OR FUNCTION('FORMAT', c.thoiGianBatDau, 'HH:mm') >= :startTimeStr) AND " +
           "(:endTimeStr IS NULL OR FUNCTION('FORMAT', c.thoiGianKetThuc, 'HH:mm') <= :endTimeStr)")
    Page<CaLamViec> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("startTimeStr") String startTimeStr,
            @Param("endTimeStr") String endTimeStr,
            Pageable pageable
    );
}