package org.example.yourchoiceshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@AllArgsConstructor // Quan trọng
@NoArgsConstructor  // Quan trọng: Bắt buộc phải có để Jackson parse JSON
@Data
public class BulkUpdateVariantRequest {
    private Integer id; // ID của biến thể cần sửa
    private BigDecimal giaBan;
    private Integer soLuong;
    // Bạn có thể thêm cân nặng hoặc các trường khác nếu muốn sửa nhanh
}