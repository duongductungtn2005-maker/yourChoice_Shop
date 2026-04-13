package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatisticFilterRequest {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Integer branchId; // Tạm thời để trống cũng được nếu bạn chưa làm bảng chi nhánh
    private String channel;   // Sẽ nhận giá trị "ONLINE" hoặc "TAI_QUAY"
    private Integer status = 4; // Mặc định bạn chỉ lấy đơn hàng đã HOAN_THANH (trạng thái = 4)
    private Integer page = 0;
    private Integer size = 10;
    private Integer threshold;
}