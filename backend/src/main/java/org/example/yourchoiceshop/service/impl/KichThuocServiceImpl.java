package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.KichThuoc;
import org.example.yourchoiceshop.repository.KichThuocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KichThuocServiceImpl {
    private final KichThuocRepository repository;

    public Page<KichThuoc> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public KichThuoc getById(Integer id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id)); }

    public KichThuoc create(StoreAttributeRequest req) {
        KichThuoc entity = new KichThuoc();
        entity.setMaKichThuoc(req.getMa() != null ? req.getMa() : "KT" + System.currentTimeMillis());
        entity.setTenKichThuoc(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public KichThuoc update(Integer id, StoreAttributeRequest req) {
        KichThuoc entity = getById(id);
        entity.setTenKichThuoc(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public void delete(Integer id) {
        KichThuoc entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}