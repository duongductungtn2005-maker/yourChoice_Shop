package org.example.yourchoiceshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietDonHangResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private String loaiHoaDon;
    private Integer trangThai;
    private LocalDateTime ngayTao;

    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;

    private List<SanPhamHoaDonResponse> sanPhamList;
    private List<LichSuHoaDonResponse> lichSuHoaDon;
    private List<LichSuThanhToanResponse> lichSuThanhToan;
    public void setLichSuDonHang(List<LichSuHoaDonResponse> list) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLichSuDonHang'");
    }

}
