package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DotGiamGiaRequest {
    private String maDotGiamGia;
    private String tenDotGiamGia;
    private BigDecimal giaTriGiam;
    private String loaiGiamGia; // "%" hoặc "VND"
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer trangThai;

    // Danh sách ID của ChiTietSanPham muốn áp dụng giảm giá
    private List<Integer> idChiTietSanPhams;
    private List<Integer> idSanPhams;
}