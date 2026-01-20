package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.XuatXu;
import org.example.yourchoiceshop.repository.XuatXuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class XuatXuServiceImpl {
    private final XuatXuRepository repository;

    public Page<XuatXu> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public XuatXu getById(Integer id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id)); }

    public XuatXu create(StoreAttributeRequest req) {
        XuatXu entity = new XuatXu();
        entity.setMaXuatXu(req.getMa() != null ? req.getMa() : "XX" + System.currentTimeMillis());
        entity.setTenXuatXu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public XuatXu update(Integer id, StoreAttributeRequest req) {
        XuatXu entity = getById(id);
        entity.setTenXuatXu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public void delete(Integer id) {
        XuatXu entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}