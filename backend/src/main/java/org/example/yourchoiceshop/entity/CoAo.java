package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
@Entity @Table(name = "co_ao") @Data
=======
@Entity
@Table(name = "co_ao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class CoAo extends BaseStatusEntity {
    @Column(name = "ma_co_ao") private String maCoAo;
    @Column(name = "ten_co_ao") private String tenCoAo;
}