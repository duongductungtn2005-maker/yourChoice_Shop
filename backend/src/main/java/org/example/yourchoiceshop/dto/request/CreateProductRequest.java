package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateProductRequest {
    private String maSanPham;
    private String tenSanPham;
    private String moTa;

    // --- THÊM DÒNG NÀY ĐỂ FIX LỖI ---
    private Integer trangThai;
    // -------------------------------

    private Integer idThuongHieu;
    private Integer idChatLieu;
    private Integer idCoAo;
    private Integer idTayAo;
    private Integer idXuatXu;
    private List<ProductVariantRequest> variants;
}