package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "co_ao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoAo extends BaseStatusEntity {
    
    @Column(name = "ma_co_ao")
    private String maCoAo;
    
    @Column(name = "ten_co_ao")
    private String tenCoAo;
}