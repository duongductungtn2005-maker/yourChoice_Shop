package org.example.yourchoiceshop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatisticRepositoryImpl implements StatisticRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RevenueSummaryDTO getRevenueSummary(StatisticFilterRequest filter) {
        // 1. Khởi tạo câu SQL cơ bản
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   COALESCE(SUM(hd.tong_tien_sau_giam), 0) as totalRevenue, " +
                        "   COUNT(hd.id) as totalOrders " +
                        "FROM hoa_don hd " +
                        "WHERE hd.trang_thai = :status "
        );

        // 2. Nối thêm điều kiện nếu Frontend có gửi lên
        if (filter.getFromDate() != null) {
            sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        }
        if (filter.getToDate() != null) {
            sql.append(" AND hd.ngay_thanh_toan <= :toDate ");
        }
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) {
            sql.append(" AND hd.loai_hoa_don = :channel ");
        }

        // 3. Tạo Query và truyền tham số an toàn
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        // 4. Lấy kết quả và map vào DTO
        Object[] result = (Object[]) query.getSingleResult();
        BigDecimal totalRevenue = result[0] != null ? new BigDecimal(result[0].toString()) : BigDecimal.ZERO;
        Long totalOrders = result[1] != null ? ((Number) result[1]).longValue() : 0L;
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RevenueSummaryDTO(totalRevenue, totalOrders, averageOrderValue, 0.0);
        // 0.0 là phần trăm tăng trưởng, bạn có thể tính thêm sau nếu muốn
    }

    @Override
    public List<RevenueChartDTO> getRevenueChart(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   CAST(hd.ngay_thanh_toan AS DATE) as thoiGian, " +
                        "   COALESCE(SUM(hd.tong_tien_sau_giam), 0) as doanhThu, " +
                        "   COUNT(hd.id) as soDonHang " +
                        "FROM hoa_don hd " +
                        "WHERE hd.trang_thai = :status "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_thanh_toan <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY CAST(hd.ngay_thanh_toan AS DATE) ");
        sql.append(" ORDER BY thoiGian ASC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<RevenueChartDTO> chartData = new ArrayList<>();

        for (Object[] row : rows) {
            // 1. Kiểm tra null cho ngày thanh toán (Nếu null thì gán chữ "Chưa xác định")
            String date = row[0] != null ? row[0].toString() : "Chưa xác định";

            // 2. Kiểm tra null cho doanh thu (Đề phòng data rác)
            BigDecimal revenue = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;

            // 3. Số đơn hàng
            Long orderCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            chartData.add(new RevenueChartDTO(date, revenue, orderCount));
        }

        return chartData;
    }
    // 1. CẬP NHẬT HÀM: Top Sản Phẩm Bán Chạy (Có Kích cỡ + Ảnh)
    @Override
    public List<ProductStatDTO> getProductStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   sp.ma_san_pham as maSP, " +
                        "   sp.ten_san_pham as tenSP, " +
                        "   COALESCE(SUM(hdct.so_luong), 0) as soLuongBan, " +
                        "   COALESCE(SUM(hdct.thanh_tien), 0) as doanhThu, " +
                        "   kt.ten_kich_thuoc as kichCo, " +
                        "   MAX(ha.duong_dan_anh) as anh " +
                        "FROM hoa_don_chi_tiet hdct " +
                        "JOIN hoa_don hd ON hdct.id_hoa_don = hd.id " +
                        "JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id " +
                        "JOIN san_pham sp ON ctsp.id_san_pham = sp.id " +
                        "LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id " +
                        "LEFT JOIN hinh_anh ha ON ctsp.id = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1 " +
                        "WHERE hd.trang_thai = :status "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_thanh_toan <= :toDate ");

        sql.append(" GROUP BY sp.ma_san_pham, sp.ten_san_pham, kt.ten_kich_thuoc ");
        sql.append(" ORDER BY soLuongBan DESC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());
        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());

        List<Object[]> rows = query.getResultList();
        List<ProductStatDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            String maSP = row[0] != null ? row[0].toString() : "";
            String tenSP = row[1] != null ? row[1].toString() : "";
            Long soLuongBan = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal doanhThu = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            String kichCo = row[4] != null ? row[4].toString() : "";
            String anh = row[5] != null ? row[5].toString() : "";
            resultList.add(new ProductStatDTO(maSP, tenSP, soLuongBan, doanhThu, kichCo, anh));
        }
        return resultList;
    }

    // 2. HÀM MỚI: Dữ liệu Biểu đồ tròn (Trạng thái đơn hàng)
    public List<OrderStatusDTO> getOrderStatusStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT trang_thai, COUNT(id) FROM hoa_don WHERE 1=1 "
        );
        if (filter.getFromDate() != null) sql.append(" AND ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND ngay_tao <= :toDate ");

        sql.append(" GROUP BY trang_thai");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());

        List<Object[]> rows = query.getResultList();
        List<OrderStatusDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            Integer trangThai = row[0] != null ? ((Number) row[0]).intValue() : 0;
            Long soLuong = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            resultList.add(new OrderStatusDTO(trangThai, soLuong));
        }
        return resultList;
    }

    // 3. HÀM MỚI: Sản phẩm sắp hết hàng (Dưới 10 cái)
    public List<ProductStatDTO> getLowStockStats() {
        String sql = "SELECT sp.ma_san_pham, sp.ten_san_pham, ctsp.so_luong, ctsp.gia_ban, kt.ten_kich_thuoc, MAX(ha.duong_dan_anh) " +
                "FROM chi_tiet_san_pham ctsp " +
                "JOIN san_pham sp ON ctsp.id_san_pham = sp.id " +
                "LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id " +
                "LEFT JOIN hinh_anh ha ON ctsp.id = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1 " +
                "WHERE ctsp.so_luong <= 10 AND ctsp.trang_thai = 1 " +
                "GROUP BY sp.ma_san_pham, sp.ten_san_pham, ctsp.so_luong, ctsp.gia_ban, kt.ten_kich_thuoc " +
                "ORDER BY ctsp.so_luong ASC";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> rows = query.getResultList();
        List<ProductStatDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            String maSP = row[0] != null ? row[0].toString() : "";
            String tenSP = row[1] != null ? row[1].toString() : "";
            Long soLuongTon = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal giaBan = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            String kichCo = row[4] != null ? row[4].toString() : "";
            String anh = row[5] != null ? row[5].toString() : "";
            resultList.add(new ProductStatDTO(maSP, tenSP, soLuongTon, giaBan, kichCo, anh));
        }
        return resultList;
    }
    @Override
    public List<EmployeeStatDTO> getEmployeeStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   nv.ma_nhan_vien as maNV, " +
                        "   nv.ten_nhan_vien as tenNV, " +
                        "   COUNT(hd.id) as tongSoDon, " +
                        "   COALESCE(SUM(hd.tong_tien_sau_giam), 0) as tongDoanhThu " +
                        "FROM hoa_don hd " +
                        "JOIN nhan_vien nv ON hd.id_nhan_vien = nv.id " +
                        "WHERE hd.trang_thai = :status "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_thanh_toan <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY nv.ma_nhan_vien, nv.ten_nhan_vien ");
        sql.append(" ORDER BY tongDoanhThu DESC "); // Sắp xếp ai doanh thu cao lên đầu

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<EmployeeStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maNV = row[0].toString();
            String tenNV = row[1].toString();
            Long tongSoDon = ((Number) row[2]).longValue();
            BigDecimal tongDoanhThu = new BigDecimal(row[3].toString());
            resultList.add(new EmployeeStatDTO(maNV, tenNV, tongSoDon, tongDoanhThu));
        }

        return resultList;
    }
    @Override
    public List<CustomerStatDTO> getCustomerStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   COALESCE(kh.ma_khach_hang, 'KHACH_LE') as maKH, " +
                        "   COALESCE(kh.ten_khach_hang, N'Khách Lẻ') as tenKH, " +
                        "   COUNT(hd.id) as tongSoDon, " +
                        "   COALESCE(SUM(hd.tong_tien_sau_giam), 0) as tongChiTieu " +
                        "FROM hoa_don hd " +
                        "LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id " +
                        "WHERE hd.trang_thai = :status "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_thanh_toan <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        // Gom nhóm theo khách hàng
        sql.append(" GROUP BY kh.id, kh.ma_khach_hang, kh.ten_khach_hang ");
        sql.append(" ORDER BY tongChiTieu DESC "); // Sắp xếp ai chi nhiều tiền nhất lên đầu

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<CustomerStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maKH = row[0].toString();
            String tenKH = row[1].toString();
            Long tongSoDon = ((Number) row[2]).longValue();
            BigDecimal tongChiTieu = new BigDecimal(row[3].toString());
            resultList.add(new CustomerStatDTO(maKH, tenKH, tongSoDon, tongChiTieu));
        }

        return resultList;
    }
    @Override
    public List<VoucherStatDTO> getVoucherStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   pgg.ma_phieu_giam_gia as maVoucher, " +
                        "   pgg.ten_phieu_giam_gia as tenVoucher, " +
                        "   COUNT(hd.id) as soLuotSuDung, " +
                        "   COALESCE(SUM(hd.tien_giam_gia), 0) as tongTienGiam " +
                        "FROM hoa_don hd " +
                        "JOIN phieu_giam_gia pgg ON hd.id_phieu_giam_gia = pgg.id " +
                        "WHERE hd.trang_thai = :status "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_thanh_toan >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_thanh_toan <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        // Gom nhóm theo Voucher
        sql.append(" GROUP BY pgg.id, pgg.ma_phieu_giam_gia, pgg.ten_phieu_giam_gia ");
        sql.append(" ORDER BY tongTienGiam DESC "); // Sắp xếp theo tổng tiền giảm nhiều nhất

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<VoucherStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maVoucher = row[0].toString();
            String tenVoucher = row[1].toString();
            Long soLuotSuDung = ((Number) row[2]).longValue();
            BigDecimal tongTienGiam = new BigDecimal(row[3].toString());
            resultList.add(new VoucherStatDTO(maVoucher, tenVoucher, soLuotSuDung, tongTienGiam));
        }

        return resultList;
    }
    @Override
    public List<DiscountCampaignStatDTO> getDiscountCampaignStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "   dgg.ma_dot_giam_gia as maCampaign, " +
                        "   dgg.ten_dot_giam_gia as tenCampaign, " +
                        "   COUNT(DISTINCT hd.id) as tongSoDon, " +
                        "   COALESCE(SUM(hdct.thanh_tien), 0) as tongDoanhThu " +
                        "FROM dot_giam_gia dgg " +
                        "JOIN chi_tiet_dot_giam_gia ctdgg ON dgg.id = ctdgg.id_dot_giam_gia " +
                        "JOIN hoa_don_chi_tiet hdct ON ctdgg.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham " +
                        "JOIN hoa_don hd ON hdct.id_hoa_don = hd.id " +
                        "WHERE hd.trang_thai = :status " +
                        "  AND hd.ngay_thanh_toan >= dgg.ngay_bat_dau " +
                        "  AND hd.ngay_thanh_toan <= dgg.ngay_ket_thuc "
        );

        // Lọc theo khoảng thời gian của đợt giảm giá
        if (filter.getFromDate() != null) sql.append(" AND dgg.ngay_bat_dau >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND dgg.ngay_ket_thuc <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY dgg.id, dgg.ma_dot_giam_gia, dgg.ten_dot_giam_gia ");
        sql.append(" ORDER BY tongDoanhThu DESC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus());

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<DiscountCampaignStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maCampaign = row[0].toString();
            String tenCampaign = row[1].toString();
            Long tongSoDon = ((Number) row[2]).longValue();
            BigDecimal tongDoanhThu = new BigDecimal(row[3].toString());
            resultList.add(new DiscountCampaignStatDTO(maCampaign, tenCampaign, tongSoDon, tongDoanhThu));
        }

        return resultList;
    }
}