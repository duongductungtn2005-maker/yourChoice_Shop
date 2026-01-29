package org.example.yourchoiceshop.dto.request;

import lombok.*;
import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;

@Getter
@Setter
public class QuanLyDonHangRequest {

    private String keyword;        // tìm theo mã HD / tên KH
    private LoaiHoaDon loaiHoaDon;  // ONLINE / TAI_QUAY
    private Integer trangThai;     // 0,1,2...
    private Integer page = 0;
    private Integer size = 10;
}
