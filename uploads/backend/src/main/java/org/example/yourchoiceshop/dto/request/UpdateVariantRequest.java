package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List; // Import List
@Data
public class UpdateVariantRequest {
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer soLuong;
    private Integer trangThai;

    // Thuộc tính biến thể
    private Integer idMauSac;
    private Integer idKichThuoc;

    // THÊM: Thuộc tính sản phẩm cha (Cho phép sửa từ Modal)
    private Integer idThuongHieu;
    private Integer idChatLieu;
    private Integer idCoAo;
    private Integer idTayAo;
    private Integer idXuatXu;
    private String moTa;
    private List<String> listAnh;
}