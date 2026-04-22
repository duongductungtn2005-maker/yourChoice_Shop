package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import thêm cái này

@Entity 
@Table(name = "nhan_vien") 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter
public class NhanVien extends BaseStatusEntity {
    
    @Column(name = "ma_nhan_vien") 
    private String maNhanVien;
    
    @Column(name = "ten_nhan_vien") 
    private String tenNhanVien;
    
    @JsonIgnore // BẮT BUỘC: Giấu mật khẩu, không bao giờ trả về qua API
    private String matKhau;
    
    private String email;
    
    @Column(name = "so_dien_thoai") 
    private String soDienThoai;
    
    private Boolean gioiTinh;
    
    @Column(name = "ngay_sinh") 
    private LocalDate ngaySinh;
    
    @Column(name = "dia_chi") 
    private String diaChi;
    
    @Column(name = "anh_dai_dien") 
    private String anhDaiDien; 

    @Column(name = "cccd")
    private String cccd;
    
    @Column(name = "ten_tai_khoan")
    private String tenTaiKhoan;

    @JsonIgnore // BẮT BUỘC: Ngắt vòng lặp JSON từ QuyenHan -> NhanVien
    @ManyToOne 
    @JoinColumn(name = "id_quyen_han") 
    private QuyenHan quyenHan;
}