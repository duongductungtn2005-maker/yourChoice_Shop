package org.example.yourchoiceshop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;

    @JsonFormat(pattern = "dd/MM/yyyy") // Format ngày sinh chuẩn VN
    private LocalDate ngaySinh;

    private Boolean gioiTinh;
    private Integer trangThai;
}