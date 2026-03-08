package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatDTO {
    private String anh;
    private String tenSanPham;
    private String kichCo;
    private BigDecimal doanhThu;
    private Long soLuongBan;
}