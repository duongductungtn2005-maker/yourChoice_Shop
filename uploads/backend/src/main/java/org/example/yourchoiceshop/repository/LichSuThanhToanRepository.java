package org.example.yourchoiceshop.repository;

import java.util.List;

import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {
    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);
}
