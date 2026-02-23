package org.example.yourchoiceshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Import lớp SanPhamHoaDonResponse độc lập (nếu cùng package thì không cần import, nhưng cứ để cho chắc)
import org.example.yourchoiceshop.dto.response.SanPhamHoaDonResponse;

@Data
public class HoaDonDetailResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String loaiHoaDon;
    private Integer trangThai;
    private LocalDateTime ngayTao;

    private BigDecimal tongTien;
    private BigDecimal tienGiam; // Đã sửa tên biến cho khớp Service
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;
    private BigDecimal tongTienHang;

    private String sdtKhachHang;
    private String diaChi;
    private String ghiChu;

    private ThongTinNhanHang thongTinNhanHang;
    private List<LichSuThanhToanResponse> lichSuThanhToan;

    // Bây giờ nó sẽ dùng class SanPhamHoaDonResponse độc lập
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

        public static LichSuThanhToanResponse fromEntity(org.example.yourchoiceshop.entity.LichSuThanhToan entity) {
            LichSuThanhToanResponse dto = new LichSuThanhToanResponse();
            dto.setSoTien(entity.getSoTien());
            dto.setNgayThanhToan(entity.getNgayThanhToan());
            dto.setHinhThucThanhToan(entity.getHinhThucThanhToan());
            return dto;
        }
    }

    // --- ĐÃ XÓA CLASS SanPhamHoaDonResponse Ở ĐÂY ĐỂ DÙNG CLASS ĐỘC LẬP ---
}