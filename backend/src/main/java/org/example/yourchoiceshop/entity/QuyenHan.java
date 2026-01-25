package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "quyen_han") @Data
public class QuyenHan extends PrimaryEntity {
    @Column(name = "ten_quyen_han") private String tenQuyenHan;
    private Integer trangThai;
}