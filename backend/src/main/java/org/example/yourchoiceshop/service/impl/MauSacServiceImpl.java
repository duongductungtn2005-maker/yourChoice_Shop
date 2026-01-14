package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.MauSac;
import org.example.yourchoiceshop.repository.MauSacRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MauSacServiceImpl {
    private final MauSacRepository repository;

    public Page<MauSac> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public MauSac getById(Integer id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id)); }

    public MauSac create(StoreAttributeRequest req) {
        MauSac entity = new MauSac();
        entity.setMaMauSac(req.getMa() != null ? req.getMa() : "MS" + System.currentTimeMillis());
        entity.setTenMauSac(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public MauSac update(Integer id, StoreAttributeRequest req) {
        MauSac entity = getById(id);
        entity.setTenMauSac(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public void delete(Integer id) {
        MauSac entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}