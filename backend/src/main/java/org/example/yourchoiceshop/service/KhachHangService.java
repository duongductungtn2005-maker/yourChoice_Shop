package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface KhachHangService {
    Page<KhachHang> findAll(String keyword, Boolean gender, Integer status, Pageable pageable);
    KhachHang findById(Integer id);
    KhachHang create(KhachHangRequest request);
    KhachHang update(Integer id, KhachHangRequest req);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    boolean existsByUsername(String username);
    boolean authenticateCustomer(String username, String password);

    // Thêm hàm này
    ByteArrayInputStream exportToExcel(String keyword, Boolean gender, Integer status) throws IOException;
    List<KhachHang> findAllList(String keyword, Boolean gender, Integer status);
}