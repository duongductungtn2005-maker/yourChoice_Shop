package org.example.yourchoiceshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreAttributeRequest {
    // Mã (Backend tự sinh nếu null)
    private String ma;

    @NotBlank(message = "Tên không được để trống")
    private String ten;

    private Integer trangThai = 1;
}