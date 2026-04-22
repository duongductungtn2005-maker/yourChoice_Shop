package org.example.yourchoiceshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tay_ao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TayAo extends BaseStatusEntity {
    @Column(name = "ma_tay_ao") private String maTayAo;
    @Column(name = "ten_tay_ao") private String tenTayAo;
    @OneToMany(mappedBy = "tayAo") // <-- ĐÚNG: Phải dùng tên biến trỏ về Tay Áo trong SanPham
    @JsonIgnore
    private List<SanPham> sanPhams;
}