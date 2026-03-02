package org.example.yourchoiceshop.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private String email;

    private BigDecimal tongTien;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTienSauGiam;

    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer idChiTietSanPham;
        private Integer soLuong;
        private Double donGia;
    }
}