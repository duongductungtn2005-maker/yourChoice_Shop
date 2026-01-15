package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "kich_thuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KichThuoc extends BaseStatusEntity {
    @Column(name = "ma_kich_thuoc") private String maKichThuoc;
    @Column(name = "ten_kich_thuoc") private String tenKichThuoc;
}