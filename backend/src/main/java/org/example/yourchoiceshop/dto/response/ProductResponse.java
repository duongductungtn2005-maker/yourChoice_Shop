package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private LocalDateTime ngayTao;
    private Integer soLuong;
    private Integer trangThai;

    // Các trường TÊN (để hiển thị ra bảng)
    private String tenThuongHieu;
    private String tenChatLieu;
    private String tenXuatXu;
    private String tenCoAo;
    private String tenTayAo;
    private String dsMauSac;
    private String dsKichThuoc;

    // --- CÁC TRƯỜNG MỚI THÊM (Để binding vào Modal sửa) ---
    private Integer idThuongHieu;
    private Integer idChatLieu;
    private Integer idXuatXu;
    private Integer idCoAo;
    private Integer idTayAo;
    private String moTa;
}