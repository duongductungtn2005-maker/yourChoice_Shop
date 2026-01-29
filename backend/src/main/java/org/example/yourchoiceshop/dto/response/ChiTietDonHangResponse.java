package org.example.yourchoiceshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietDonHangResponse {

    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String loaiHoaDon;
    private Integer trangThai;
    private LocalDateTime ngayTao;

    private BigDecimal tongTienSauGiam;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;

    private List<SanPhamTrongDonResponse> sanPhamList;
    private List<LichSuThanhToanResponse> lichSuThanhToan;
    private ThongTinNhanHangResponse thongTinNhanHang;
}
