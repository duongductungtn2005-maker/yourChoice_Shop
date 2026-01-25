package org.example.yourchoiceshop.service;

import java.util.List;

import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhanVienService {
    Page<NhanVien> findAll(String keyword, Boolean gender, Integer status, Pageable pageable);  
    NhanVien create(EmployeeRequest request);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    NhanVien findById(Integer id);
    NhanVien update(Integer id, EmployeeRequest req);
    List<NhanVien> findAllList(String keyword, Boolean gender, Integer status);
}