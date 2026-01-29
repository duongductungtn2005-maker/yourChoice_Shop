package org.example.yourchoiceshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGiaRequest {

    private String maPhieuGiamGia;
    
    private String tenPhieuGiamGia;
    
    // "PhanTram" hoặc "TienMat"
    private String loaiPhieu; 
    
    // "CongKhai" hoặc "CaNhan" (MỚI)
    private String kieu; 
    
    private BigDecimal giaTriGiam;
    
    private BigDecimal giaTriGiamToiDa;
    
    private BigDecimal donHangToiThieu;
    
    private Integer soLuong;
    
    private LocalDateTime ngayBatDau;
    
    private LocalDateTime ngayKetThuc;
    
    private Integer trangThai;

    // Danh sách ID khách hàng để gửi mail (Chỉ dùng khi kieu = "CaNhan")
    private List<Integer> customerIds;
}