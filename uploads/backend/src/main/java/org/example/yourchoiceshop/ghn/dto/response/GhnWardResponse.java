package org.example.yourchoiceshop.ghn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GhnWardResponse {
    private String wardCode;
    private String wardName;
    private Integer districtId;
}