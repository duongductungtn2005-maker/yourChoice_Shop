package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerStatDTO {
    private String maKhachHang;
    private String tenKhachHang;
    private Long tongSoDon;
    private BigDecimal tongChiTieu;
}