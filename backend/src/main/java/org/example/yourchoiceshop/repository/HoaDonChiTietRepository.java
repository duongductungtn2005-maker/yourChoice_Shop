package org.example.yourchoiceshop.repository;

import java.util.List;
import org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse;
import org.example.yourchoiceshop.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("""
        SELECT new org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse(
            hdct.id,
            sp.tenSanPham,
            ms.tenMauSac,
            kt.tenKichThuoc,
            hdct.soLuong,
            hdct.donGia,
            hdct.thanhTien,
            CAST(NULL as string)
        )
        FROM HoaDonChiTiet hdct
        JOIN hdct.chiTietSanPham ctsp
        JOIN ctsp.sanPham sp
        JOIN ctsp.mauSac ms
        JOIN ctsp.kichThuoc kt
        WHERE hdct.hoaDon.id = :idHoaDon
    """)
    List<SanPhamHoaDonResponse> findSanPhamByDonHang(@Param("idHoaDon") Integer idHoaDon);

    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);
}