package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_knowledge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatKnowledge extends PrimaryEntity {

    @Column(name = "cau_hoi_mau", columnDefinition = "NVARCHAR(500)", nullable = false)
    private String cauHoiMau;

    @Column(name = "tu_khoa", columnDefinition = "NVARCHAR(500)")
    private String tuKhoa;

    @Column(name = "cau_tra_loi", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String cauTraLoi;

    @Column(name = "do_uu_tien")
    private Integer doUuTien;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "so_lan_su_dung")
    private Integer soLanSuDung;

    @Column(name = "lan_su_dung_cuoi")
    private LocalDateTime lanSuDungCuoi;

    @PrePersist
    public void prePersist() {
        if (doUuTien == null) doUuTien = 0;
        if (trangThai == null) trangThai = true;
        if (soLanSuDung == null) soLanSuDung = 0;
    }
}
