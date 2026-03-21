package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.CaLamViec;
import org.example.yourchoiceshop.entity.LichLamViec;
import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {
    List<LichLamViec> findByNgayLamViecBetweenOrderByNgayLamViecAsc(LocalDate startDate, LocalDate endDate);
    List<LichLamViec> findAllByOrderByNgayLamViecAsc();
    boolean existsByNhanVienAndCaLamViecAndNgayLamViec(NhanVien nhanVien, CaLamViec caLamViec, LocalDate ngayLamViec);
    List<LichLamViec> findByNgayLamViecBetween(LocalDate startDate, LocalDate endDate);
    // Đổi l.nhanVien.username thành l.nhanVien.tenTaiKhoan
@Query("SELECT l FROM LichLamViec l WHERE l.nhanVien.tenTaiKhoan = :username AND l.ngayLamViec = :ngayLamViec")
Optional<LichLamViec> findLichCuaNhanVienTrongNgay(
        @Param("username") String username, 
        @Param("ngayLamViec") LocalDate ngayLamViec
);

    // Thêm phương thức findById
    Optional<LichLamViec> findById(Integer id);
}
