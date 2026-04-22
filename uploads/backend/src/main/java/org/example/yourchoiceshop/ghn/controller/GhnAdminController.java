package org.example.yourchoiceshop.ghn.controller;


import lombok.RequiredArgsConstructor;

import org.example.yourchoiceshop.ghn.dto.request.GhnTinhPhiRequest;
import org.example.yourchoiceshop.ghn.dto.response.GhnDistrictResponse;
import org.example.yourchoiceshop.ghn.dto.response.GhnProvinceResponse;
import org.example.yourchoiceshop.ghn.dto.response.GhnTinhPhiResponse;
import org.example.yourchoiceshop.ghn.dto.response.GhnWardResponse;
import org.example.yourchoiceshop.ghn.service.GhnService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ghn")
public class GhnAdminController {

    private final GhnService ghnService;

    @GetMapping("/tinh-thanh")
    public List<GhnProvinceResponse> getTinhThanh() {
        return ghnService.layDanhSachTinhThanh();
    }

    @GetMapping("/quan-huyen/{provinceId}")
    public List<GhnDistrictResponse> getQuanHuyen(@PathVariable Integer provinceId) {
        return ghnService.layDanhSachQuanHuyen(provinceId);
    }

    @GetMapping("/phuong-xa/{districtId}")
    public List<GhnWardResponse> getPhuongXa(@PathVariable Integer districtId) {
        return ghnService.layDanhSachPhuongXa(districtId);
    }

    @GetMapping("/dia-chi-shop")
    public Map<String, Object> getDiaChiShop() {
        return ghnService.layDiaChiShopMacDinh();
    }

    @PostMapping("/tinh-phi-van-chuyen")
    public GhnTinhPhiResponse tinhPhiVanChuyen(@RequestBody GhnTinhPhiRequest request) {
        return ghnService.tinhPhiVanChuyen(request);
    }
}