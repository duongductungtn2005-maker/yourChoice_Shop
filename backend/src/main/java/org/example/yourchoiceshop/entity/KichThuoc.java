package org.example.yourchoiceshop.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "kich_thuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KichThuoc extends BaseStatusEntity {
    @Column(name = "ma_kich_thuoc") private String maKichThuoc;
    @Column(name = "ten_kich_thuoc") private String tenKichThuoc;
    @OneToMany(mappedBy = "kichThuoc", fetch = FetchType.LAZY)
    @JsonIgnore // <--- BẮT BUỘC
    private List<ChiTietSanPham> chiTietSanPhams;
}