package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
@Entity @Table(name = "kich_thuoc") @Data
=======
@Entity
@Table(name = "kich_thuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class KichThuoc extends BaseStatusEntity {
    @Column(name = "ma_kich_thuoc") private String maKichThuoc;
    @Column(name = "ten_kich_thuoc") private String tenKichThuoc;
}