package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name = "phuong_thuc_thanh_toan") @Data
public class PhuongThucThanhToan extends PrimaryEntity {
    @Column(name = "ma_phuong_thuc") private String maPhuongThuc;
    @Column(name = "ten_phuong_thuc") private String tenPhuongThuc;
    private String ghiChu;
    private Integer trangThai;
}