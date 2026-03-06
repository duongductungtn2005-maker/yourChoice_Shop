package org.example.yourchoiceshop.service;

import java.util.List;

import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NhanVienService {
    // Trong Interface NhanVienService
    Page<NhanVien> findAll(String keyword, Integer status, String role, Pageable pageable);
    NhanVien create(EmployeeRequest request);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    NhanVien findById(Integer id);
    NhanVien update(Integer id, EmployeeRequest req);
    List<NhanVien> findAllList(String keyword, Boolean gender, Integer status);
    boolean checkTrungTaiKhoan(String tenTaiKhoan, Integer id);
    boolean checkTrungSoDienThoai(String soDienThoai, Integer id);
    boolean authenticateEmployee(String username, String password);
}