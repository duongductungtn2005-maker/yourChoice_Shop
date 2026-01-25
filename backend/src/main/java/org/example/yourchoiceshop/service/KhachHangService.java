package org.example.yourchoiceshop.service;

import java.util.List;

import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KhachHangService {
    Page<KhachHang> findAll(String keyword, Boolean gender, Integer status, Pageable pageable);  
    KhachHang create(KhachHangRequest request);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    KhachHang findById(Integer id);
    KhachHang update(Integer id, KhachHangRequest req);
    List<KhachHang> findAllList(String keyword, Boolean gender, Integer status);
}