package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {
    List<LichLamViec> findByNgayLamViecBetweenOrderByNgayLamViecAsc(LocalDate startDate, LocalDate endDate);
    List<LichLamViec> findAllByOrderByNgayLamViecAsc();
    boolean existsByNhanVienAndCaLamViecAndNgayLamViec(NhanVien nhanVien, CaLamViec caLamViec, LocalDate ngayLamViec);
}