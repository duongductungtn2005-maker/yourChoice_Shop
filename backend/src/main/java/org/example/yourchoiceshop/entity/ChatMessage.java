package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends PrimaryEntity {

    /**
     * Vai trò người gửi: "CUSTOMER", "AI", "STAFF", "SYSTEM"
     */
    @Column(name = "sender_role")
    private String senderRole;

    /**
     * Tên hiển thị của người gửi
     */
    @Column(name = "sender_name")
    private String senderName;

    /**
     * Nội dung tin nhắn
     */
    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    /**
     * Loại tin nhắn: "TEXT", "PRODUCT_LIST" (gợi ý sản phẩm)
     */
    @Column(name = "loai_tin_nhan")
    private String loaiTinNhan;

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_session")
    private ChatSession chatSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @PrePersist
    public void prePersist() {
        if (this.ngayGui == null) this.ngayGui = LocalDateTime.now();
        if (this.loaiTinNhan == null) this.loaiTinNhan = "TEXT";
    }
}
