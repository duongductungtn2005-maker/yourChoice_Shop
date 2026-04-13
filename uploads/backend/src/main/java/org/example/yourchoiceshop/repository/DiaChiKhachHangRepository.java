package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {

    // Lấy danh sách địa chỉ của khách hàng (chỉ lấy cái đang hoạt động)
    List<DiaChiKhachHang> findByKhachHangIdAndTrangThai(Integer khachHangId, Integer trangThai);

    // Lấy tất cả địa chỉ của khách hàng (để xử lý logic reset mặc định)
    List<DiaChiKhachHang> findByKhachHangId(Integer khachHangId);
}