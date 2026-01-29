package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.response.ProductSimpleResponse; // Import DTO mới
import org.example.yourchoiceshop.entity.SanPham;
import org.example.yourchoiceshop.repository.SanPhamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl {

    private final SanPhamRepository sanPhamRepository;

    // Sửa kiểu trả về thành List<ProductSimpleResponse>
    public List<ProductSimpleResponse> getAllActive() {
        List<SanPham> listEntities = sanPhamRepository.findAll();
        List<ProductSimpleResponse> listResponses = new ArrayList<>();

        for (SanPham sp : listEntities) {
            // Chỉ lấy sản phẩm có trạng thái = 1
            if (sp.getTrangThai() != null && sp.getTrangThai() == 1) {
                // Sử dụng Constructor của DTO mới (chỉ 3 tham số)
                listResponses.add(new ProductSimpleResponse(
                        sp.getId(),
                        sp.getMaSanPham(),
                        sp.getTenSanPham()
                ));
            }
        }
        return listResponses;
    }
}