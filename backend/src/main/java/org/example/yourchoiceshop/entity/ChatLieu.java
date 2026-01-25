package org.example.yourchoiceshop.entity;
import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
@Entity @Table(name = "chat_lieu") @Data
=======
@Entity
@Table(name = "chat_lieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> upstream/main
public class ChatLieu extends BaseStatusEntity {
    @Column(name = "ma_chat_lieu") private String maChatLieu;
    @Column(name = "ten_chat_lieu") private String tenChatLieu;
}