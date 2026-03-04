package org.example.yourchoiceshop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BulkUpdateWrapper {
    // Đây là cái vỏ bọc danh sách
    private List<BulkUpdateVariantRequest> variants;
}