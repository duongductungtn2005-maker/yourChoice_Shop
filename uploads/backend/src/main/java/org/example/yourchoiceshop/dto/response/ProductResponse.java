package org.example.yourchoiceshop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    // Giá sau giảm min (nếu có đợt giảm giá đang hoạt động)
    private BigDecimal giaSauGiamMin;
    // % giảm tối đa (dùng để hiển thị badge)
    private BigDecimal phanTramGiamMax;
    // Ảnh đại diện sản phẩm (lấy từ biến thể đầu tiên)
    private String anhChinh;
    // Danh sách tất cả ảnh của các biến thể (dùng cho slider)
    private List<String> dsAnh;
}