package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
@Entity @Table(name = "thuong_hieu") @Data
=======
@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class ThuongHieu extends BaseStatusEntity {
    @Column(name = "ma_thuong_hieu") private String maThuongHieu;
    @Column(name = "ten_thuong_hieu") private String tenThuongHieu;
}