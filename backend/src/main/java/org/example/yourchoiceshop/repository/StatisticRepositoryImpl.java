package org.example.yourchoiceshop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.example.yourchoiceshop.dto.request.StatisticFilterRequest;
import org.example.yourchoiceshop.dto.response.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatisticRepositoryImpl implements StatisticRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RevenueSummaryDTO getRevenueSummary(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        // index 0: Thực tế (Chỉ tính trạng thái 5)
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai = 5 THEN (hd.tong_tien_sau_giam - COALESCE(hd.phi_van_chuyen, 0)) ELSE 0 END), 0) AS totalRevenue, " +
                        // index 1: Dự kiến (Tính trạng thái 1, 2, 3, 4, 5 - không tính 0 hủy và 6 trả hàng)
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai IN (1,2,3,4,5) THEN (hd.tong_tien_sau_giam - COALESCE(hd.phi_van_chuyen, 0)) ELSE 0 END), 0) AS expectedRevenue, " +
                        "  COUNT(hd.id) AS totalOrders, " +
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai = 5 THEN 1 ELSE 0 END), 0) AS successOrders, " +
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai IN (1,2,3,4) THEN 1 ELSE 0 END), 0) AS processingOrders, " +
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai = 0 THEN 1 ELSE 0 END), 0) AS cancelOrders, " +
                        "  COALESCE(SUM(CASE WHEN hd.trang_thai = 6 THEN 1 ELSE 0 END), 0) AS returnOrders " +
                        "FROM hoa_don hd WHERE 1=1 "
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_tao <= :toDate ");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());

        Object[] result = (Object[]) query.getSingleResult();

        // Query phụ để đếm tổng số lượng sản phẩm đã bán (chỉ tính đơn hoàn thành)
        StringBuilder sqlProducts = new StringBuilder(
                "SELECT COALESCE(SUM(hdct.so_luong), 0) FROM hoa_don_chi_tiet hdct " +
                        "JOIN hoa_don hd ON hdct.id_hoa_don = hd.id WHERE hd.trang_thai = 5 "
        );
        if (filter.getFromDate() != null) sqlProducts.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sqlProducts.append(" AND hd.ngay_tao <= :toDate ");

        Query queryProd = entityManager.createNativeQuery(sqlProducts.toString());
        if (filter.getFromDate() != null) queryProd.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) queryProd.setParameter("toDate", filter.getToDate());

        Long totalProducts = ((Number) queryProd.getSingleResult()).longValue();

        RevenueSummaryDTO dto = new RevenueSummaryDTO();
        // Cập nhật lại Index mảng do mình vừa chèn thêm cột expectedRevenue vào giữa
        dto.setTotalRevenue(result[0] != null ? new BigDecimal(result[0].toString()) : BigDecimal.ZERO);
        dto.setExpectedRevenue(result[1] != null ? new BigDecimal(result[1].toString()) : BigDecimal.ZERO); // ĐÃ THÊM
        dto.setTotalOrders(result[2] != null ? ((Number) result[2]).longValue() : 0L);
        dto.setSuccessOrders(result[3] != null ? ((Number) result[3]).longValue() : 0L);
        dto.setProcessingOrders(result[4] != null ? ((Number) result[4]).longValue() : 0L);
        dto.setCancelOrders(result[5] != null ? ((Number) result[5]).longValue() : 0L);
        dto.setReturnOrders(result[6] != null ? ((Number) result[6]).longValue() : 0L);
        
        dto.setTotalProducts(totalProducts);
        dto.setGrowthPercent(0.0); 

        return dto;
    }

    @Override
    public List<RevenueChartDTO> getRevenueChart(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  DAY(ngay_tao) AS date, " +
                        "  COALESCE(SUM(tong_tien_sau_giam - COALESCE(phi_van_chuyen, 0)), 0) AS value " +
                        "FROM hoa_don " +
                        "WHERE trang_thai = 5 "
        );

        if (filter.getFromDate() != null) sql.append(" AND ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND ngay_tao <= :toDate ");

        sql.append(" GROUP BY DAY(ngay_tao) ORDER BY DAY(ngay_tao) ");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());

        List<Object[]> rows = query.getResultList();
        List<RevenueChartDTO> chartData = new ArrayList<>();

        for (Object[] row : rows) {
            String date = row[0] != null ? row[0].toString() : "0";
            BigDecimal value = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            chartData.add(new RevenueChartDTO(date, value));
        }

        return chartData;
    }

    @Override
    public List<ProductStatDTO> getProductStats(StatisticFilterRequest filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  (SELECT TOP 1 ha.duong_dan_anh FROM hinh_anh ha WHERE ha.id_chi_tiet_san_pham = ctsp.id ORDER BY ha.anh_chinh DESC, ha.id ASC) AS anh, " +
                        "  sp.ten_san_pham AS tenSanPham, " +
                        "  kt.ten_kich_thuoc AS kichCo, " +
                        "  SUM(hdct.so_luong * hdct.don_gia) AS doanhThu, " +
                        "  SUM(hdct.so_luong) AS soLuongBan " +
                        "FROM hoa_don_chi_tiet hdct " +
                        "JOIN hoa_don hd ON hdct.id_hoa_don = hd.id " +
                        "JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id " +
                        "JOIN san_pham sp ON ctsp.id_san_pham = sp.id " +
                        "LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id " +
                        "WHERE hd.trang_thai = 5 " 
        );

        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_tao <= :toDate ");

        sql.append(" GROUP BY ctsp.id, sp.ten_san_pham, kt.ten_kich_thuoc ");
        sql.append(" ORDER BY soLuongBan DESC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());

        List<Object[]> rows = query.getResultList();
        List<ProductStatDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            ProductStatDTO dto = new ProductStatDTO();
            dto.setAnh(row[0] != null ? row[0].toString() : null);
            dto.setTenSanPham(row[1] != null ? row[1].toString() : "Không xác định");
            dto.setKichCo(row[2] != null ? row[2].toString() : "");
            dto.setDoanhThu(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO);
            dto.setSoLuongBan(row[4] != null ? ((Number) row[4]).longValue() : 0L);
            result.add(dto);
        }

        return result;
    }

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

    @Override
    public List<ProductStatDTO> getLowStockStats(StatisticFilterRequest filter) {
        int threshold = (filter != null && filter.getThreshold() != null) ? filter.getThreshold() : 10;

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        // Subquery lấy ảnh an toàn kể cả khi không có chi tiết SP
                        "  (SELECT TOP 1 ha.duong_dan_anh FROM hinh_anh ha WHERE ha.id_chi_tiet_san_pham = ctsp.id ORDER BY ha.anh_chinh DESC, ha.id ASC) AS anh, " +
                        "  sp.ten_san_pham AS tenSanPham, " +
                        "  COALESCE(kt.ten_kich_thuoc, 'N/A') AS kichCo, " +
                        "  COALESCE(ctsp.gia_ban, 0) AS doanhThu, " +
                        // Bọc COALESCE: Nếu SP chưa có chi tiết, số lượng = 0
                        "  COALESCE(ctsp.so_luong, 0) AS soLuongBan " +
                        "FROM san_pham sp " +
                        // Đổi thành LEFT JOIN lấy SP làm gốc
                        "LEFT JOIN chi_tiet_san_pham ctsp ON ctsp.id_san_pham = sp.id " +
                        "LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id " +
                        // Điều kiện: SP đang kinh doanh (số 1) VÀ tồn kho <= ngưỡng
                        "WHERE sp.trang_thai = 1 " + 
                        "AND COALESCE(ctsp.so_luong, 0) <= :threshold " +
                        "ORDER BY COALESCE(ctsp.so_luong, 0) ASC "
        );

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("threshold", threshold);

        List<Object[]> rows = query.getResultList();
        List<ProductStatDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            ProductStatDTO dto = new ProductStatDTO();
            dto.setAnh(row[0] != null ? row[0].toString() : null);
            dto.setTenSanPham(row[1] != null ? row[1].toString() : "Không xác định");
            dto.setKichCo(row[2] != null ? row[2].toString() : "");
            dto.setDoanhThu(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO);
            
            // Hứng giá trị SỐ LƯỢNG TỒN vào biến soLuongBan của DTO
            dto.setSoLuongBan(row[4] != null ? ((Number) row[4]).longValue() : 0L); 
            result.add(dto);
        }

        return result;
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

        // Đã sửa thành ngay_tao để đồng bộ
        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_tao <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY nv.ma_nhan_vien, nv.ten_nhan_vien ");
        sql.append(" ORDER BY tongDoanhThu DESC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus() != null ? filter.getStatus() : 5); // Đảm bảo luôn có status

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<EmployeeStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maNV = row[0] != null ? row[0].toString() : "N/A";
            String tenNV = row[1] != null ? row[1].toString() : "N/A";
            Long tongSoDon = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal tongDoanhThu = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
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

        // Đã sửa thành ngay_tao
        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_tao <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY kh.id, kh.ma_khach_hang, kh.ten_khach_hang ");
        sql.append(" ORDER BY tongChiTieu DESC "); 

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus() != null ? filter.getStatus() : 5);

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<CustomerStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maKH = row[0] != null ? row[0].toString() : "KHACH_LE";
            String tenKH = row[1] != null ? row[1].toString() : "Khách lẻ";
            Long tongSoDon = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal tongChiTieu = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
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

        // Đã sửa thành ngay_tao
        if (filter.getFromDate() != null) sql.append(" AND hd.ngay_tao >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND hd.ngay_tao <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY pgg.id, pgg.ma_phieu_giam_gia, pgg.ten_phieu_giam_gia ");
        sql.append(" ORDER BY tongTienGiam DESC "); 

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus() != null ? filter.getStatus() : 5);

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<VoucherStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maVoucher = row[0] != null ? row[0].toString() : "N/A";
            String tenVoucher = row[1] != null ? row[1].toString() : "N/A";
            Long soLuotSuDung = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal tongTienGiam = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
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
                        "  AND hd.ngay_tao >= dgg.ngay_bat_dau " +
                        "  AND hd.ngay_tao <= dgg.ngay_ket_thuc "
        );

        if (filter.getFromDate() != null) sql.append(" AND dgg.ngay_bat_dau >= :fromDate ");
        if (filter.getToDate() != null) sql.append(" AND dgg.ngay_ket_thuc <= :toDate ");
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) sql.append(" AND hd.loai_hoa_don = :channel ");

        sql.append(" GROUP BY dgg.id, dgg.ma_dot_giam_gia, dgg.ten_dot_giam_gia ");
        sql.append(" ORDER BY tongDoanhThu DESC ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("status", filter.getStatus() != null ? filter.getStatus() : 5);

        if (filter.getFromDate() != null) query.setParameter("fromDate", filter.getFromDate());
        if (filter.getToDate() != null) query.setParameter("toDate", filter.getToDate());
        if (filter.getChannel() != null && !filter.getChannel().isEmpty()) query.setParameter("channel", filter.getChannel());

        List<Object[]> rows = query.getResultList();
        List<DiscountCampaignStatDTO> resultList = new ArrayList<>();

        for (Object[] row : rows) {
            String maCampaign = row[0] != null ? row[0].toString() : "N/A";
            String tenCampaign = row[1] != null ? row[1].toString() : "N/A";
            Long tongSoDon = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal tongDoanhThu = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            resultList.add(new DiscountCampaignStatDTO(maCampaign, tenCampaign, tongSoDon, tongDoanhThu));
        }

        return resultList;
    }
}