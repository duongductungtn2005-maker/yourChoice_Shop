package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "mau_sac") @Data
public class MauSac extends BaseStatusEntity {
    @Column(name = "ma_mau_sac") private String maMauSac;
    @Column(name = "ten_mau_sac") private String tenMauSac;
}