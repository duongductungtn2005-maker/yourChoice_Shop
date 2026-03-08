package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List; // Thêm import List

import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class PhieuGiamGiaRequest {
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