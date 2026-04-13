package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherStatDTO {
    private String maVoucher;
    private String tenVoucher;
    private Long soLuotSuDung;    // Số lần mã này được áp dụng thành công
    private BigDecimal tongTienGiam; // Tổng số tiền shop đã giảm cho mã này
}