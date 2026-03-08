package org.example.yourchoiceshop.repository;

import org.example.yourchoiceshop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer>, JpaSpecificationExecutor<KhachHang> {

    // =========================
    // SEARCH (JPQL)
    // =========================
    @Query("SELECT n FROM KhachHang n WHERE " +
            "(:keyword IS NULL OR " +
            " LOWER(n.tenKhachHang) LIKE :keyword OR " +
            " LOWER(n.email) LIKE :keyword OR " +
            " n.soDienThoai LIKE :keyword) " +
            "AND (:gender IS NULL OR n.gioiTinh = :gender) " +
            "AND (:status IS NULL OR n.trangThai = :status)")
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("gender") Boolean gender,
            @Param("status") Integer status,
            Pageable pageable
    );

    // =========================
    // CHECK TRÙNG TÀI KHOẢN / LOGIN
    // =========================
    boolean existsByTenTaiKhoanIgnoreCase(String tenTaiKhoan);

    boolean existsByTenTaiKhoanIgnoreCaseAndMatKhau(String tenTaiKhoan, String matKhau);

    boolean existsByTenTaiKhoanIgnoreCaseAndIdNot(String tenTaiKhoan, Integer id);

    // =========================
    // CHECK TRÙNG EMAIL / SĐT
    // =========================
    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    // Dùng cho UPDATE (trừ chính nó ra)
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    // =========================
    // THỐNG KÊ (NATIVE QUERY)
    // =========================
    @Query(value = "SELECT " +
            "k.id AS id, " +
            "k.ten_khach_hang AS tenKhachHang, " +
            "k.so_dien_thoai AS soDienThoai, " +
            "k.email AS email, " +
            "k.ngay_sinh AS ngaySinh, " +
            "k.trang_thai AS trangThai, " +
            "COALESCE(SUM(h.tong_tien), 0) AS tongChiTieu, " +
            "COUNT(h.id) AS soDonHang, " +
            "MAX(h.ngay_tao) AS donHangGanNhat " +
            "FROM khach_hang k " +
            "LEFT JOIN hoa_don h ON k.id = h.id_khach_hang " +
            "WHERE (:keyword = '' OR LOWER(k.ten_khach_hang) LIKE :keyword OR LOWER(k.email) LIKE :keyword OR k.so_dien_thoai LIKE :keyword) " +
            "AND (:status = -1 OR k.trang_thai = :status) " +
            "GROUP BY k.id, k.ten_khach_hang, k.so_dien_thoai, k.email, k.ngay_sinh, k.trang_thai",
            countQuery = "SELECT COUNT(k.id) FROM khach_hang k " +
                    "WHERE (:keyword = '' OR LOWER(k.ten_khach_hang) LIKE :keyword OR LOWER(k.email) LIKE :keyword OR k.so_dien_thoai LIKE :keyword) " +
                    "AND (:status = -1 OR k.trang_thai = :status)",
            nativeQuery = true)
    Page<KhachHangThongKeRes> searchKhachHangThongKe(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );

    // =========================
    // PROJECTION KẾT QUẢ THỐNG KÊ
    // =========================
    interface KhachHangThongKeRes {
        Integer getId();
        String getTenKhachHang();
        String getSoDienThoai();
        String getEmail();
        Date getNgaySinh();
        Integer getTrangThai();
        Number getTongChiTieu();
        Number getSoDonHang();
        Date getDonHangGanNhat();
    }
}