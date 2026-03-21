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
import java.util.List;
import java.util.Optional;

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

    Optional<KhachHang> findByTenTaiKhoanIgnoreCaseAndMatKhau(String tenTaiKhoan, String matKhau);

    boolean existsByTenTaiKhoanIgnoreCaseAndIdNot(String tenTaiKhoan, Integer id);

    // =========================
    // CHECK TRÙNG EMAIL / SĐT
    // =========================
    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    // =========================
    // THỐNG KÊ (NATIVE QUERY)
    // Tính cả đơn mua online/theo tài khoản và đơn tại quầy qua SĐT
    // Loại bỏ đơn hủy (trang_thai != 0)
    // =========================
    @Query(value = "SELECT " +
            "k.id AS id, " +
            "k.ten_khach_hang AS tenKhachHang, " +
            "k.so_dien_thoai AS soDienThoai, " +
            "k.email AS email, " +
            "k.ngay_sinh AS ngaySinh, " +
            "k.trang_thai AS trangThai, " +
            "(SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) " +
            "   FROM hoa_don hd " +
            "  WHERE (hd.id_khach_hang = k.id OR hd.sdt_nguoi_nhan = k.so_dien_thoai) " +
            "    AND hd.trang_thai != 0) AS tongChiTieu, " +
            "(SELECT COUNT(hd.id) " +
            "   FROM hoa_don hd " +
            "  WHERE (hd.id_khach_hang = k.id OR hd.sdt_nguoi_nhan = k.so_dien_thoai) " +
            "    AND hd.trang_thai != 0) AS soDonHang, " +
            "(SELECT MAX(hd.ngay_tao) " +
            "   FROM hoa_don hd " +
            "  WHERE (hd.id_khach_hang = k.id OR hd.sdt_nguoi_nhan = k.so_dien_thoai) " +
            "    AND hd.trang_thai != 0) AS donHangGanNhat " +
            "FROM khach_hang k " +
            "WHERE (:keyword = '' " +
            "   OR LOWER(k.ten_khach_hang) LIKE :keyword " +
            "   OR LOWER(k.email) LIKE :keyword " +
            "   OR k.so_dien_thoai LIKE :keyword) " +
            "AND (:status = -1 OR k.trang_thai = :status) " +
            "ORDER BY k.id DESC",
            countQuery = "SELECT COUNT(k.id) " +
                    "FROM khach_hang k " +
                    "WHERE (:keyword = '' " +
                    "   OR LOWER(k.ten_khach_hang) LIKE :keyword " +
                    "   OR LOWER(k.email) LIKE :keyword " +
                    "   OR k.so_dien_thoai LIKE :keyword) " +
                    "AND (:status = -1 OR k.trang_thai = :status)",
            nativeQuery = true)
    Page<KhachHangThongKeRes> searchKhachHangThongKe(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );

    // =========================
    // PHỤC VỤ GỬI VOUCHER / PHÂN NHÓM KHÁCH HÀNG
    // =========================

    // Lấy tất cả ID khách hàng đang hoạt động
    @Query("SELECT k.id FROM KhachHang k WHERE k.trangThai = 1")
    List<Integer> findAllActiveCustomerIds();

    // Lấy ID khách hàng theo khoảng chi tiêu
    @Query(value = "SELECT k.id " +
            "FROM khach_hang k " +
            "WHERE k.trang_thai = 1 " +
            "AND (SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) " +
            "       FROM hoa_don hd " +
            "      WHERE (hd.id_khach_hang = k.id OR hd.sdt_nguoi_nhan = k.so_dien_thoai) " +
            "        AND hd.trang_thai != 0) >= :minSpend " +
            "AND (SELECT COALESCE(SUM(hd.tong_tien_sau_giam), 0) " +
            "       FROM hoa_don hd " +
            "      WHERE (hd.id_khach_hang = k.id OR hd.sdt_nguoi_nhan = k.so_dien_thoai) " +
            "        AND hd.trang_thai != 0) < :maxSpend",
            nativeQuery = true)
    List<Integer> findCustomerIdsBySpendRange(
            @Param("minSpend") Long minSpend,
            @Param("maxSpend") Long maxSpend
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