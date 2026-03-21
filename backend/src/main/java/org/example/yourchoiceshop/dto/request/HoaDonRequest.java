package org.example.yourchoiceshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonRequest {
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiNguoiNhan;
    private Integer nhanVienId;
    // Bạn có thể thêm email hoặc ghi chú nếu muốn sửa thêm
    private String ghiChu; // <--- THÊM DÒNG NÀY ĐỂ FIX LỖI getGhiChu()
}
