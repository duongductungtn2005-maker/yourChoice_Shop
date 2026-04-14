package org.example.yourchoiceshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThuongHieu extends BaseStatusEntity {
    @Column(name = "ma_thuong_hieu") private String maThuongHieu;
    @Column(name = "ten_thuong_hieu") private String tenThuongHieu;
    @OneToMany(mappedBy = "thuongHieu") // <-- ĐÚNG: Phải dùng tên biến trỏ về Tay Áo trong SanPham
    @JsonIgnore
    private List<SanPham> sanPhams;
}