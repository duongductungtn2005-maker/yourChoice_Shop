package org.example.yourchoiceshop.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String tenTaiKhoan;
    private String matKhau;
}
