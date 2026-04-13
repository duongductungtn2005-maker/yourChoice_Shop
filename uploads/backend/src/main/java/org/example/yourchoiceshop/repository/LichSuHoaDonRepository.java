package org.example.yourchoiceshop.repository;

import java.util.List;

import org.example.yourchoiceshop.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    List<LichSuHoaDon> findByHoaDonIdOrderByThoiGianAsc(Integer hoaDonId);
}
