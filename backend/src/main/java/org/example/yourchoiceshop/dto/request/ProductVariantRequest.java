package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantRequest {
    private Integer idMauSac;
    private Integer idKichThuoc;
    private Integer soLuong;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private List<String> listAnh;
}