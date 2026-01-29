package org.example.yourchoiceshop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.yourchoiceshop.entity.enums.LoaiHoaDon;

import lombok.*;


@Getter
@Setter
public class QuanLyDonHangResponse {

    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private BigDecimal tongTienSauGiam;
    private LocalDateTime ngayTao;
    private LoaiHoaDon loaiHoaDon;
    private Integer trangThai;


    public QuanLyDonHangResponse(
            Integer id,
            String maHoaDon,
            String tenKhachHang,
            BigDecimal tongTienSauGiam,
            LocalDateTime ngayTao,
            LoaiHoaDon loaiHoaDon,
            Integer trangThai
    ) {
        this.id = id;
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.tongTienSauGiam = tongTienSauGiam;
        this.ngayTao = ngayTao;
        this.loaiHoaDon = loaiHoaDon;
        this.trangThai = trangThai;
    }
}