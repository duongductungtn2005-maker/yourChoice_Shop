// package org.example.yourchoiceshop.repository;

// import java.util.List;

// import org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse;
// import org.example.yourchoiceshop.entity.HoaDonChiTiet;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// @Repository
// public interface ChiTietDonHangRepository
//         extends JpaRepository<HoaDonChiTiet, Integer> {

//     @Query("""
//                 SELECT new org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse(
//                     sp.tenSanPham,
//                     kt.tenKichThuoc,
//                     ms.tenMauSac,
//                     hdct.soLuong,
//                     hdct.donGia,
//                     (hdct.donGia * hdct.soLuong)
//                 )
//                 FROM HoaDonChiTiet hdct
//                 JOIN hdct.chiTietSanPham ctsp
//                 JOIN ctsp.sanPham sp
//                 LEFT JOIN ctsp.kichThuoc kt
//                 LEFT JOIN ctsp.mauSac ms
//                 WHERE hdct.hoaDon.id = :hoaDonId
//             """)
//     List<SanPhamHoaDonResponse> findSanPhamByDonHang(
//             @Param("hoaDonId") Integer hoaDonId);

// }
