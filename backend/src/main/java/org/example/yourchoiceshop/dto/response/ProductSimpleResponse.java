package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimpleResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
}