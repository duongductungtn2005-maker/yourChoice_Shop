package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "ca_lam_viec")
@Data
public class CaLamViec extends BaseStatusEntity {

    @Column(name = "ma_ca")
    private String maCa;

    @Column(name = "ten_ca")
    private String tenCa;

    @Column(name = "thoi_gian_bat_dau")
    private LocalTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc")
    private LocalTime thoiGianKetThuc;

    @Column(name = "ghi_chu")
    private String ghiChu;
}