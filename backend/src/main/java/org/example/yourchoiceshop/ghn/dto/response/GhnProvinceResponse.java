package org.example.yourchoiceshop.ghn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GhnProvinceResponse {
    private Integer provinceId;
    private String provinceName;
}