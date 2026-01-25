package org.example.yourchoiceshop.repository;



import org.example.yourchoiceshop.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    // Query tìm kiếm nâng cao: Từ khóa + Trạng thái + Kiểu (Công khai/Cá nhân)
    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
            "(:keyword IS NULL OR p.maPhieuGiamGia LIKE %:keyword% OR p.tenPhieuGiamGia LIKE %:keyword%) " +
            "AND (:status IS NULL OR p.trangThai = :status) " +
            "AND (:scope IS NULL OR p.kieu = :scope) " +
            "ORDER BY p.id DESC")
    Page<PhieuGiamGia> search(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("scope") String scope,
            Pageable pageable
    );

    boolean existsByMaPhieuGiamGia(String ma);
}