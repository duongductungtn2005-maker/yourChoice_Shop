package org.example.yourchoiceshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "xuat_xu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class XuatXu extends BaseStatusEntity {
    @Column(name = "ma_xuat_xu") private String maXuatXu;
    @Column(name = "ten_xuat_xu") private String tenXuatXu;
    @OneToMany(mappedBy = "xuatXu") // <-- ĐÚNG: Phải dùng tên biến trỏ về Tay Áo trong SanPham
    @JsonIgnore
    private List<SanPham> sanPhams;
}