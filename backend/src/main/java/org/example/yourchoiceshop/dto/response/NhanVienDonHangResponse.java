package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NhanVienDonHangResponse {
    private Integer id;
    private String maNhanVien;
    private String tenNhanVien;
    private String soDienThoai;
    private String email;
}
