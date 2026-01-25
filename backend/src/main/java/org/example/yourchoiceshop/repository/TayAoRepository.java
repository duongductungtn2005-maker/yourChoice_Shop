package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.TayAo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TayAoRepository extends JpaRepository<TayAo, Integer> {
    @Query("SELECT x FROM TayAo x WHERE " +
            "(:keyword IS NULL OR x.tenTayAo LIKE %:keyword%) " +
            "AND (:status IS NULL OR x.trangThai = :status)")
    Page<TayAo> search(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
}