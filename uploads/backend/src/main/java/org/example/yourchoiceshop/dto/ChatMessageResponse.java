package org.example.yourchoiceshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Integer id;
    private Integer sessionId;
    private Integer nhanVienId;
    private String senderRole;
    private String senderName;
    private String noiDung;
    private String loaiTinNhan;
    private LocalDateTime ngayGui;
}
