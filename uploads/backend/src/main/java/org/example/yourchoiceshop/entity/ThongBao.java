package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "loai")
    private String loai;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "da_doc")
    private Boolean daDoc;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
