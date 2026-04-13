package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO;
import org.example.yourchoiceshop.repository.GiaoCaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GiaoCaService {
    
    private final GiaoCaRepository repo;

    public Page<LichSuHoatDongDTO> getLichSu(String search, String tenCa, 
                                             LocalDate startDate, LocalDate endDate, 
                                             Pageable pageable) {
        return repo.findLichSuHoatDong(search, tenCa, startDate, endDate, pageable);
    }
}