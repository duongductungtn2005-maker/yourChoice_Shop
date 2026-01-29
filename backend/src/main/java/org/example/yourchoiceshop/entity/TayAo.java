package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tay_ao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TayAo extends BaseStatusEntity {
    @Column(name = "ma_tay_ao") private String maTayAo;
    @Column(name = "ten_tay_ao") private String tenTayAo;
}