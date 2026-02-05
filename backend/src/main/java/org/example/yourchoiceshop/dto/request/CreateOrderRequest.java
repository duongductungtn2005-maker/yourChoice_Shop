package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String tenKhachHang; // "Khách lẻ" hoặc tên khách chọn
    private List<CartItem> items; // Danh sách sản phẩm trong giỏ

    @Data
    public static class CartItem {
        private Integer idSanPham; // ID của Chi tiết sản phẩm
        private Integer soLuong;
        private Double donGia;
    }
}