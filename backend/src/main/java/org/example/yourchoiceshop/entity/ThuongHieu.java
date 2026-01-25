package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThuongHieu extends BaseStatusEntity {
    @Column(name = "ma_thuong_hieu") private String maThuongHieu;
    @Column(name = "ten_thuong_hieu") private String tenThuongHieu;
}