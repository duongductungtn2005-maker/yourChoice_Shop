package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
// Thêm thư viện này của Jackson
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 

@Entity
@Table(name = "lich_lam_viec")
@Data
public class LichLamViec extends BaseStatusEntity {

    // 1. ĐỔI LAZY THÀNH EAGER ĐỂ VUE LẤY ĐƯỢC TÊN NHÂN VIÊN
    // 2. THÊM @JsonIgnoreProperties ĐỂ CHỐNG LỖI LẶP VÔ HẠN JSON
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "id_nhan_vien")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lichLamViecs"}) // lichLamViecs là tên list bên entity NhanVien (nếu có)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ca_lam_viec")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lichLamViecs"})
    private CaLamViec caLamViec;

    @Column(name = "ngay_lam_viec")
    private LocalDate ngayLamViec;

    @Column(name = "ghi_chu")
    private String ghiChu;
}