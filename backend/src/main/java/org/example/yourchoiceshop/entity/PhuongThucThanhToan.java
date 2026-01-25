package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
<<<<<<< HEAD
import lombok.Data;
@Entity @Table(name = "phuong_thuc_thanh_toan") @Data
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "phuong_thuc_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class PhuongThucThanhToan extends PrimaryEntity {
    @Column(name = "ma_phuong_thuc") private String maPhuongThuc;
    @Column(name = "ten_phuong_thuc") private String tenPhuongThuc;
    private String ghiChu;
    private Integer trangThai;
}