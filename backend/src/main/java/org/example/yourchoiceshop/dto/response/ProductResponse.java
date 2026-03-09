package org.example.yourchoiceshop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;

    // Định dạng ngày giờ trả về cho Frontend
    // Kết quả sẽ là chuỗi: "26/01/2026"
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime ngayTao;

    private Integer soLuong;
    private Integer trangThai;

    // --- Các trường TÊN (để hiển thị ra bảng) ---
    private String tenThuongHieu;
    private String tenChatLieu;
    private String tenXuatXu;
    private String tenCoAo;
    private String tenTayAo;
    private String dsMauSac;
    private String dsKichThuoc;

    // --- CÁC TRƯỜNG ID (Để binding vào Modal sửa) ---
    private Integer idThuongHieu;
    private Integer idChatLieu;
    private Integer idXuatXu;
    private Integer idCoAo;
    private Integer idTayAo;
    private String moTa;

    // Giá bán min/max từ các biến thể
    private BigDecimal giaBanMin;
    private BigDecimal giaBanMax;
    // Ảnh đại diện sản phẩm (lấy từ biến thể đầu tiên)
    private String anhChinh;
}