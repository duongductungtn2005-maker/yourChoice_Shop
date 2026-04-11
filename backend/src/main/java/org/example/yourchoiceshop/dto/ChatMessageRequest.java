package org.example.yourchoiceshop.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Integer sessionId;
    private String sessionKey; // for anonymous users
    private String noiDung;
    private String senderRole; // CUSTOMER, STAFF
    private String senderName;
    private Integer khachHangId;
    private Integer nhanVienId;
}
