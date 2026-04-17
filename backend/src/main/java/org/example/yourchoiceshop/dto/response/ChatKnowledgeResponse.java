package org.example.yourchoiceshop.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatKnowledgeResponse {
    private Integer id;
    private String cauHoiMau;
    private String tuKhoa;
    private String cauTraLoi;
    private Integer doUuTien;
    private Boolean trangThai;
    private Integer soLanSuDung;
}
