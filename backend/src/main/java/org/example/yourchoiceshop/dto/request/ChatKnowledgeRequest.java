package org.example.yourchoiceshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatKnowledgeRequest {

    @NotBlank(message = "Câu hỏi mẫu không được để trống")
    private String cauHoiMau;

    private String tuKhoa;

    @NotBlank(message = "Câu trả lời không được để trống")
    private String cauTraLoi;

    private Integer doUuTien;

    private Boolean trangThai;
}
