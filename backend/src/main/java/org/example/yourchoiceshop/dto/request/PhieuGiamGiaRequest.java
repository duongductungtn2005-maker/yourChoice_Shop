package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PhieuGiamGiaRequest {
    private Integer id;
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String loaiPhieu; // "PhanTram" hoặc "TienMat"
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriGiamToiDa;
    private BigDecimal donHangToiThieu;
    private Integer soLuong;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer trangThai;
    private String kieu;
    private String moTa;
    private List<Integer> customerIds; // Danh sách ID khách hàng
}