package org.example.yourchoiceshop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;

    // Tên hiển thị của các thuộc tính
    private String tenThuongHieu;
    private String tenXuatXu;
    private String tenChatLieu;
    private String tenCoAo;
    private String tenTayAo;

    private Integer tongSoLuongTon; // Tổng số lượng của tất cả biến thể
    private Integer soLuongBienThe; // Có bao nhiêu biến thể
    private BigDecimal giaThapNhat; // Khoảng giá
    private BigDecimal giaCaoNhat;

    private Integer trangThai;
    private LocalDateTime ngayTao;
}