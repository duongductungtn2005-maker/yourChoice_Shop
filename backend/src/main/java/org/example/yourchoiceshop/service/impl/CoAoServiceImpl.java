package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.CoAo;
import org.example.yourchoiceshop.repository.CoAoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoAoServiceImpl {
    private final CoAoRepository repository;

    public Page<CoAo> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public CoAo getById(Integer id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id)); }

    public CoAo create(StoreAttributeRequest req) {
        CoAo entity = new CoAo();
        entity.setMaCoAo(req.getMa() != null ? req.getMa() : "CA" + System.currentTimeMillis());
        entity.setTenCoAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public CoAo update(Integer id, StoreAttributeRequest req) {
        CoAo entity = getById(id);
        entity.setTenCoAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }
    public void delete(Integer id) {
        CoAo entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
}