package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.TayAo;
import org.example.yourchoiceshop.repository.TayAoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TayAoServiceImpl {
    private final TayAoRepository repository;

    public Page<TayAo> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public TayAo getById(Integer id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id)); }

    public TayAo create(StoreAttributeRequest req) {
        TayAo entity = new TayAo();
        entity.setMaTayAo(req.getMa() != null ? req.getMa() : "TA" + System.currentTimeMillis());
        entity.setTenTayAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public TayAo update(Integer id, StoreAttributeRequest req) {
        TayAo entity = getById(id);
        entity.setTenTayAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public void delete(Integer id) {
        TayAo entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}