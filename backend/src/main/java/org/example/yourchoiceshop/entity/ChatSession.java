package org.example.yourchoiceshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession extends PrimaryEntity {

    /**
     * Loại chat: "KHACH_HANG" (khách hàng) hoặc "NOI_BO" (nội bộ nhân viên)
     */
    @Column(name = "loai_chat")
    private String loaiChat;

    /**
     * Trạng thái: 1 = Đang hoạt động, 2 = Chờ nhận, 3 = Đã đóng
     */
    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    /**
     * Tên hiển thị (khách vãng lai hoặc tên khách hàng)
     */
    @Column(name = "ten_hien_thi")
    private String tenHienThi;

    /**
     * Session ID cho khách vãng lai (anonymous)
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * Ai đang xử lý: "AI" hoặc tên nhân viên
     */
    @Column(name = "nguoi_xu_ly")
    private String nguoiXuLy;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @OneToMany(mappedBy = "chatSession", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChatMessage> chatMessages;

    @PrePersist
    public void prePersist() {
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
        if (this.trangThai == null) this.trangThai = 1;
    }

    @PreUpdate
    public void preUpdate() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}
