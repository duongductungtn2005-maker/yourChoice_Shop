package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class PrimaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // --- VIẾT THỦ CÔNG ĐỂ CHẮC CHẮN JSON NHẬN ĐƯỢC ID ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}