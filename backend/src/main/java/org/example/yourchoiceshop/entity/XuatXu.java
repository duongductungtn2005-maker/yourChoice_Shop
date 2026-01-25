package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xuat_xu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class XuatXu extends BaseStatusEntity {
    @Column(name = "ma_xuat_xu") private String maXuatXu;
    @Column(name = "ten_xuat_xu") private String tenXuatXu;
}