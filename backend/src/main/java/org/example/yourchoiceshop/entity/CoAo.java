package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

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
    @OneToMany(mappedBy = "coAo")
    @JsonIgnore
    private List<SanPham> sanPhams; // Hoặc List<ChiTietSanPham>
}