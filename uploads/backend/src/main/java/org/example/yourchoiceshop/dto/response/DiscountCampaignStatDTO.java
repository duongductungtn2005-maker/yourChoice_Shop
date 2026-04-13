package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCampaignStatDTO {
    private String maCampaign;
    private String tenCampaign;
    private Long tongSoDon;      // Số đơn hàng có mua sản phẩm giảm giá
    private BigDecimal tongDoanhThu; // Doanh thu mang lại từ các sản phẩm đó
}