package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.PhieuGiamGiaCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Integer> {
    // Tìm danh sách phiếu cá nhân theo ID phiếu giảm giá cha
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGiaId(Integer idPhieu);
}