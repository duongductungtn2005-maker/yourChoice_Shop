package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "chat_lieu") @Data
public class ChatLieu extends BaseStatusEntity {
    @Column(name = "ma_chat_lieu") private String maChatLieu;
    @Column(name = "ten_chat_lieu") private String tenChatLieu;
}