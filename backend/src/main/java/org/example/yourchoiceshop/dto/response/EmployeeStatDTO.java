package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeStatDTO {
    private String maNhanVien;
    private String tenNhanVien;
    private Long tongSoDon;
    private BigDecimal tongDoanhThu;
}