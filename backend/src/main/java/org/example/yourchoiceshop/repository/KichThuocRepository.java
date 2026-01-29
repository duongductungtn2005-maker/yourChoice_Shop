package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.List;
@Repository

public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {
    @Query("SELECT x FROM KichThuoc x WHERE " +
            "(:keyword IS NULL OR x.tenKichThuoc LIKE %:keyword%) " +
            "AND (:status IS NULL OR x.trangThai = :status)")
    Page<KichThuoc> search(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);
}