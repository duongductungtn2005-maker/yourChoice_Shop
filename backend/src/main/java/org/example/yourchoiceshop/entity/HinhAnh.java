package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hinh_anh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnh extends PrimaryEntity { 

    @Column(name = "duong_dan_anh")
    private String duongDanAnh;

    @Column(name = "ten_anh")
    private String tenAnh;

    @Column(name = "anh_chinh")
    private Boolean anhChinh;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @ManyToOne
    @JoinColumn(name = "id_ctsp")
    @JsonIgnore // Ngắt chiều ngược lại từ Ảnh -> Sản phẩm chi tiết
    private ChiTietSanPham chiTietSanPham;
}