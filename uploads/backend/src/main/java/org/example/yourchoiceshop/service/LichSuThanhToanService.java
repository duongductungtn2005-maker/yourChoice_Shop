package org.example.yourchoiceshop.service;

import java.util.List;

import org.example.yourchoiceshop.entity.LichSuThanhToan;
import org.example.yourchoiceshop.repository.LichSuThanhToanRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LichSuThanhToanService {

    private final LichSuThanhToanRepository repository;

    public List<LichSuThanhToan> getByHoaDonId(Integer hoaDonId) {
        return repository.findByHoaDonId(hoaDonId);
    }
}