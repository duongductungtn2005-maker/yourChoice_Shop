package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateProductRequest {
    private String maSanPham;
    private String tenSanPham;
    private String moTa;

    // Các ID thuộc tính
    private Integer idThuongHieu;
    private Integer idXuatXu;
    private Integer idChatLieu;
    private Integer idCoAo;
    private Integer idTayAo;

    // Danh sách biến thể
    private List<ProductVariantRequest> variants;
}