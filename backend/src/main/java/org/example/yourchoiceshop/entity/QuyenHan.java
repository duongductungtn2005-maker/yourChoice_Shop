package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
@Entity @Table(name = "quyen_han") @Data
=======
@Entity
@Table(name = "quyen_han")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class QuyenHan extends PrimaryEntity {
    @Column(name = "ten_quyen_han") private String tenQuyenHan;
    private Integer trangThai;
}