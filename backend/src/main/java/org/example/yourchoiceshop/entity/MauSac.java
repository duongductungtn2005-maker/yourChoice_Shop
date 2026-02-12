package org.example.yourchoiceshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore; // <-- ĐỪNG QUÊN IMPORT NÀY
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "mau_sac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MauSac extends BaseStatusEntity {
    @Column(name = "ma_mau_sac") private String maMauSac;
    @Column(name = "ten_mau_sac") private String tenMauSac;
    @OneToMany(mappedBy = "mauSac", fetch = FetchType.LAZY)
    @JsonIgnore // <--- BẮT BUỘC
    private List<ChiTietSanPham> chiTietSanPhams;
}