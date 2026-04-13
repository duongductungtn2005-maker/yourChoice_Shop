package org.example.yourchoiceshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionResponse {
    private Integer id;
    private String loaiChat;
    private Integer trangThai;
    private String tenHienThi;
    private String nguoiXuLy;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String lastMessage;
}
