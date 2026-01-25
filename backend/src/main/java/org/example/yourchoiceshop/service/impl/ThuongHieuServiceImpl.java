package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ThuongHieu;
import org.example.yourchoiceshop.repository.ThuongHieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThuongHieuServiceImpl {
    private final ThuongHieuRepository repository;

    public Page<ThuongHieu> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ThuongHieu getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id));
    }

    public ThuongHieu create(StoreAttributeRequest req) {
        ThuongHieu entity = new ThuongHieu();
        entity.setMaThuongHieu(req.getMa() != null ? req.getMa() : "TH" + System.currentTimeMillis());
        entity.setTenThuongHieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public ThuongHieu update(Integer id, StoreAttributeRequest req) {
        ThuongHieu entity = getById(id);
        entity.setTenThuongHieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        ThuongHieu entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}