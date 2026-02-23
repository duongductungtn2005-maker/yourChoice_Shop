package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;
import org.example.yourchoiceshop.entity.enums.TrangThaiHoaDon;

@Data
public class QuanLyDonHangRequest {

    private String keyword;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;

    private LoaiHoaDon loaiHoaDon;

    private TrangThaiHoaDon trangThai;

    private Integer page = 0;

    private Integer size = 10;
}
