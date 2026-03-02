package org.example.yourchoiceshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantResponse {
    private Integer id;
    private String maCtsp;
    private Integer soLuong;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer trangThai;

    // Trả về Object con để Frontend dùng v.mauSac.tenMauSac
    private AttributeDTO mauSac;
    private AttributeDTO kichThuoc;
    private List<String> listAnh;
    // Class nội bộ (Inner Class) để chứa thông tin thuộc tính
    @Data
    @AllArgsConstructor
    public static class AttributeDTO {
        private Integer id;
        private String ten; // Tên màu hoặc tên size

        // Để khớp với Frontend đang dùng (v.mauSac.tenMauSac), ta cần getter đặc biệt
        // Hoặc đơn giản là Frontend sửa lại thành v.mauSac.ten
        // Nhưng để KHÔNG SỬA FRONTEND, ta map thủ công như sau:
        public String getTenMauSac() { return ten; }
        public String getTenKichThuoc() { return ten; }
    }
}