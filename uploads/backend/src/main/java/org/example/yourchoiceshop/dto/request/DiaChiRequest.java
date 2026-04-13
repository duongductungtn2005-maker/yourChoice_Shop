package org.example.yourchoiceshop.dto.request;

import lombok.Data;

@Data
public class DiaChiRequest {
    private Integer idKhachHang; // ID của khách hàng sở hữu địa chỉ này

    private String tenNguoiNhan;
    private String soDienThoai;

    private String thanhPho; // Tỉnh/Thành phố
    private String quan;     // Quận/Huyện
    private String phuong;   // Phường/Xã
    private String diaChiCuThe; // Số nhà, đường...

    private Boolean macDinh; // True/False
    private Integer trangThai;
}