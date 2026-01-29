package org.example.yourchoiceshop.dto.request;
import lombok.Data;
import java.util.List;

@Data
public class CreateProductRequest {
    private String maSanPham;
    private String tenSanPham;
    private String moTa; // <--- BẠN ĐANG THIẾU DÒNG NÀY
    private Integer idThuongHieu;
    private Integer idChatLieu;
    private Integer idCoAo;
    private Integer idTayAo;
    private Integer idXuatXu;
    private List<ProductVariantRequest> variants;
}