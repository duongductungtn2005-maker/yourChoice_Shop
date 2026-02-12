package org.example.yourchoiceshop.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "chat_lieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatLieu extends BaseStatusEntity {
    
    @Column(name = "ma_chat_lieu")
    private String maChatLieu;
    
    @Column(name = "ten_chat_lieu")
    private String tenChatLieu;
    @OneToMany(mappedBy = "chatLieu", fetch = FetchType.LAZY)
    @JsonIgnore // <--- BẮT BUỘC
    private List<ChiTietSanPham> chiTietSanPhams;
    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;
}