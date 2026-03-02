package org.example.yourchoiceshop.service;

import java.sql.Timestamp;

import org.example.yourchoiceshop.dto.request.LichSuHoatDongDTO;
import org.example.yourchoiceshop.repository.GiaoCaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GiaoCaService {
    @Autowired
    private final GiaoCaRepository repo;

    public Page<LichSuHoatDongDTO> getLichSu(String search, String tenCa, Pageable pageable) {
        Page<Object[]> results = repo.findLichSuHoatDong(search, tenCa, pageable);
        
        return results.map(row -> {
            LichSuHoatDongDTO dto = new LichSuHoatDongDTO();
            dto.setId((Integer) row[0]);
            dto.setTenNhanVien((String) row[1]);
            dto.setMaNhanVien((String) row[2]);
            dto.setTenCa((String) row[3]);
            dto.setVaoCaThucTe(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null);
            dto.setRaCaThucTe(row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null);
            dto.setTrangThai((Integer) row[6]);
            dto.setGhiChu((String) row[7]);
            return dto;
        });
    }
}