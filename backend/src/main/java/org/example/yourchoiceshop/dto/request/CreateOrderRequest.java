package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    private String maHoaDon;

    private String tenKhachHang;
    private String soDienThoai;
    private String diaChiChiTiet;
    private String wardName;
    private String districtName;
    private String provinceName;
    private String email;

    private String ghiChu;

    private Integer idNhanVien;
    private Integer idKhachHang;

    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;
    private String hinhThucThanhToan;

    private List<PhieuGiamGiaRequest> phieuGiamGia;

    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer idChiTietSanPham;
        private Integer soLuong;
        private Double donGia;
    }

}