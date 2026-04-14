package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardStatisticResponse<S, C, D> {
    private S summary;           // Phần tổng quan (các con số)
    private List<C> chartData;   // Dữ liệu cho biểu đồ
    private Page<D> detailTable; // Dữ liệu cho bảng chi tiết (có phân trang)
}