package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LichLamViecRequest {
    // Đổi chữ Long thành Integer ở 2 dòng này nhé:
    private Integer caLamViecId; 
    private Integer nhanVienId;  
    
    private LocalDate ngayLamViec;
}