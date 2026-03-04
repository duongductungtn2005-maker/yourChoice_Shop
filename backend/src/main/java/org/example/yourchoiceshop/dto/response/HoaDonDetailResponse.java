package org.example.yourchoiceshop.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
public class HoaDonDetailResponse {
    // Dành cho màn chi tiết (ChiTietDonHang.vue)
    private String maHoaDon;
    private String tenKhachHang;
    private String emailKhachHang; 
    private String loaiHoaDon;
    private Integer trangThai;
    private LocalDateTime ngayTao;

    // Thông tin tài chính
    private BigDecimal tongTien;
    private BigDecimal tienGiam;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;

    private ThongTinNhanHang thongTinNhanHang;
    private List<LichSuThanhToanResponse> lichSuThanhToan;
    private List<SanPhamHoaDonResponse> sanPhamHoaDon;

    @Data
    public static class ThongTinNhanHang {
        private String tenNguoiNhan;
        private String sdt;
        private String diaChi;
    }

    @Data
    public static class LichSuThanhToanResponse {
        private BigDecimal soTien;
        private LocalDateTime ngayThanhToan;
        private String hinhThucThanhToan;
    }

    @Data
    public static class SanPhamHoaDonResponse {
        private String tenSanPham;
        private String size;
        private String mauSac;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
        private String anh; // Nếu có
    }
}