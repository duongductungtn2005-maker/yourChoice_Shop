package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String hinhThucThanhToan; // TIEN_MAT hoặc CHUYEN_KHOAN
    private BigDecimal soTien;
    private String ghiChu;
}